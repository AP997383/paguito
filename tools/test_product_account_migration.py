#!/usr/bin/env python3
"""Run the real Room 1→2→3 SQL, ownership queries and schema checks without touching installed data."""
from pathlib import Path
import argparse
import re
import sqlite3

root = Path(__file__).resolve().parents[1]
source = root / 'app/src/main/java/com/nexusystem/paguito'
parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--database', type=Path, help='Optional v1/v2 database copy; always cloned into memory.')
args = parser.parse_args()
generated = root / 'app/build/generated/source/kapt/prod/debug/com/nexusystem/paguito/data/AppDatabase_Impl.java'
assert generated.exists(), 'Compile ProdDebug before running this check.'
creates = dict(re.findall(r'"(CREATE TABLE IF NOT EXISTS `(DeudoresTable|ProductosTble|PagosTable)`[^"\n]+)"', generated.read_text()))
creates = {table: sql for sql, table in creates.items()}
assert len(creates) == 3
migration_source = (source / 'data/AppDatabase.kt').read_text()
one, two = migration_source.split('val MIGRATION_2_3', 1)
migrations = {1: re.findall(r'db.execSQL\("([^"]+)"\)', one), 2: re.findall(r'db.execSQL\("([^"]+)"\)', two)}
db = sqlite3.connect(':memory:')
if args.database:
    original = sqlite3.connect(f'file:{args.database}?mode=ro', uri=True)
    original.backup(db)
    version = db.execute('PRAGMA user_version').fetchone()[0]
    assert version in (1, 2)
else:
    version = 1
    for table, sql in creates.items():
        legacy = re.sub(r", `(?:ownerAccount|isSynced|creditBalance)` (?:TEXT|INTEGER|REAL) NOT NULL DEFAULT (?:''|0\.0|0)", '', sql)
        if table == 'PagosTable':
            legacy = legacy.replace('`montoAbonado` REAL', '`montoAbonado` INTEGER').replace('`saldoAntesDeAbono` REAL', '`saldoAntesDeAbono` INTEGER')
        db.execute(legacy)
    db.execute("INSERT INTO ProductosTble VALUES (1,'Histórico','verified','',10,15,50,'%',2,'Conservar',3)")
    db.execute("INSERT INTO DeudoresTable VALUES (1,'client',1,'Cliente','','','',-3330,100,'2026-11-19','M',0,1,'[]','Conservar')")
    db.execute("INSERT INTO PagosTable VALUES (1,'movement','client','1',15,-3330,'2026-11-19',1,2,1,'Conservar','[]')")
original_rows = {table: db.execute(f'SELECT * FROM {table} ORDER BY id').fetchall() for table in creates}
original_columns = {table: [r[1] for r in db.execute(f'PRAGMA table_info({table})')] for table in creates}
for step in range(version, 3):
    for sql in migrations[step]: db.execute(sql)
for table, sql in creates.items():
    current = db.execute(f"SELECT {','.join(original_columns[table])} FROM {table} ORDER BY id").fetchall()
    assert current == original_rows[table], f'Historical values changed in {table}'
    fresh = sqlite3.connect(':memory:'); fresh.execute(sql)
    # Room compares names and affinities, not physical column order.
    fields = lambda connection: {r[1]: tuple(r[2:]) for r in connection.execute(f'PRAGMA table_info({table})')}
    assert fields(db) == fields(fresh), f'Room schema mismatch: {table}'
    assert db.execute(f'SELECT COUNT(*) FROM {table} WHERE ownerAccount IS NULL').fetchone()[0] == 0
queries = re.findall(r'@Query\("([^"]+)"\)', (source / 'data/local/dao/ProductosDao.kt').read_text())
select = next(q for q in queries if q.startswith('SELECT *') and 'ownerAccount = :owner' in q and 'nombre' not in q)
assert not db.execute(select, {'owner': 'foreign-account'}).fetchall()
if not args.database:
    claim = next(q for q in queries if q.startswith('UPDATE') and 'remoteIds' in q)
    db.execute(claim.replace('(:remoteIds)', '(:remoteId)'), {'owner': 'account-a', 'remoteId': 'verified'})
    assert db.execute(select, {'owner': 'account-a'}).fetchone()[0] == 1
    delete = next(q for q in queries if q.startswith('DELETE'))
    db.execute(delete, {'id': 1, 'owner': 'account-b'})
    assert db.execute('SELECT COUNT(*) FROM ProductosTble').fetchone()[0] == 1
    db.execute("INSERT INTO PagosTable (idRemoteDatabase,idDeudorRemoteDatabase,idDeudor,montoAbonado,saldoAntesDeAbono,fechaAbono,pagoATiempo,tipoPago,isIngreso,notas,jsonAbonoPorProducto) VALUES ('decimal','client','1',15.75,-3330.5,'2026-11-19',1,1,1,'','[]')")
    assert db.execute("SELECT montoAbonado,saldoAntesDeAbono FROM PagosTable WHERE idRemoteDatabase='decimal'").fetchone() == (15.75, -3330.5)
assert 'index_ProductosTble_ownerAccount' in [r[1] for r in db.execute('PRAGMA index_list(ProductosTble)')]
print(f'Migration {version}→3 passed: all historical values/IDs preserved, Room schema matches, account isolation and decimal amounts verified.')
