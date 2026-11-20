package com.nexusystem.paguito.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel

/** Error compartido de pagos, ventas y eliminación: conserva el formulario para corregir o reintentar. */
@Composable
fun PaymentOperationFeedback(viewModel: PagosViewModel) {
    if (viewModel.isSaving) {
        androidx.compose.ui.window.Dialog(onDismissRequest = {}, properties = androidx.compose.ui.window.DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)) {
            androidx.compose.material3.Surface(shape = androidx.compose.material3.MaterialTheme.shapes.extraLarge) {
                androidx.compose.foundation.layout.Column(modifier = androidx.compose.ui.Modifier.padding(24.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    androidx.compose.material3.CircularProgressIndicator()
                    Text(viewModel.operationLabel)
                }
            }
        }
    }
    viewModel.saveError?.let { message ->
        AlertDialog(onDismissRequest = viewModel::clearSaveError, title = { Text("No se pudo completar") },
            text = { Text(message) }, confirmButton = { TextButton(onClick = viewModel::clearSaveError) { Text("Entendido") } })
    }
}
