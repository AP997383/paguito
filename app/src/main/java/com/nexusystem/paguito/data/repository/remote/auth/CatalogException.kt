package com.nexusystem.paguito.data.repository.remote.auth

sealed class CatalogException : Exception() {

    class InvalidResponse : CatalogException()

    class SubdomainAlreadyExists : CatalogException()

    class InvalidSubdomain : CatalogException()

    class StoreNotFound : CatalogException()

    class ServerError : CatalogException()
}