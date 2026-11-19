package com.nexusystem.paguito.ui.screens.home.components

import com.nexusystem.paguito.ui.components.GuideStep

const val HOME_GUIDE_ID = "home_v1"
val HomeGuideSteps = listOf(
    GuideStep("home-overview", "Tu negocio hoy", "Aquí puedes mirar tus ventas del día y el estado general de tu negocio: cuánto has vendido, tus productos y los saldos por cobrar."),
    GuideStep("home-actions", "Acciones rápidas", "Estas tarjetas son accesos directos a las funciones de Paguito. Registra pagos, añade clientes y productos, crea ventas o abre tu sitio sin salir de Inicio."),
    GuideStep("home-online", "Cobra en línea", "Configura tu pasarela de pago y envía enlaces de pago a tus clientes para que puedan pagarte en línea."),
    GuideStep("home-upcoming", "Próximos cobros", "Aquí aparecerán los clientes cuya fecha de pago está próxima. Revisa sus saldos y abre su perfil para dar seguimiento al cobro."),
    GuideStep("home-recent", "Actividad reciente", "En esta sección verás las ventas y los cobros que se han registrado últimamente. Abre el historial para consultar todos tus movimientos.")
)
