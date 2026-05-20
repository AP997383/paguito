package com.nexusystem.paguito.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.R
import com.nexusystem.paguito.utils.LoadingOverlay

// --- COLORES ---
val BgLightGreen = Color(0xFFE8F5E9) // Fondo verde muy claro para el ícono
val BorderColor = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit = {},
    goToOtp: (String) -> Unit = {},
    viewModel:AuthViewModel
) {
    // Estado del campo de texto
    var identifier by remember { mutableStateOf("") }
    val otpSucess by viewModel.otpSuseccfull.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    if(otpSucess.equals("TRUE")){
        goToOtp(identifier)
    }
  Box{
      Scaffold(
          topBar = {
              TopAppBar(
                  title = { },
                  navigationIcon = {
                      IconButton(onClick = onBackClick) {
                          Icon(
                              imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                              contentDescription = "Atrás",
                              tint = TextDark
                          )
                      }
                  },
                  colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                  windowInsets = WindowInsets(0.dp)
              )
          },
          containerColor = MaterialTheme.colorScheme.background
      ) { paddingValues ->
          Box {
              Column(
                  modifier = Modifier
                      .fillMaxSize()
                      .padding(paddingValues)
                      .padding(horizontal = 24.dp)
                      .imePadding(), // Empuja el botón hacia arriba cuando se abre el teclado
                  horizontalAlignment = Alignment.Start
              ) {
                  Spacer(modifier = Modifier.height(16.dp))

                  // 1. Ícono Superior (Llave)
                  Box(
                      modifier = Modifier
                          .size(56.dp)
                          .background(BgLightGreen, RoundedCornerShape(12.dp)),
                      contentAlignment = Alignment.Center
                  ) {
                      Icon(
                          imageVector = Icons.Outlined.Key,
                          contentDescription = "Recuperar contraseña",
                          tint =  MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(28.dp)
                      )
                  }

                  Spacer(modifier = Modifier.height(24.dp))

                  // 2. Textos (Título y Subtítulo)
                  Text(
                      text = "Recuperar Contraseña",
                      fontSize = 26.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = TextDark
                  )

                  Spacer(modifier = Modifier.height(12.dp))

                  Text(
                      text = "Ingresa tu correo electrónico  registrado para recibir un código de recuperación.",
                      fontSize = 15.sp,
                      color = TextGray,
                      lineHeight = 22.sp
                  )

                  Spacer(modifier = Modifier.height(32.dp))

                  // 3. Campo de Entrada
                  Text(
                      text = "Correo",
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = TextDark,
                      modifier = Modifier.padding(bottom = 8.dp)
                  )

                  OutlinedTextField(
                      value = identifier,
                      onValueChange = { identifier = it },
                      modifier = Modifier.fillMaxWidth(),
                      shape = RoundedCornerShape(12.dp),
                      singleLine = true,
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                      placeholder = {
                          Text(
                              text = "ejemplo@correo.com",
                              color = TextLightGray,
                              fontSize = 14.sp
                          )
                      },
                      leadingIcon = {
                          Icon(
                              imageVector = Icons.Outlined.Person,
                              contentDescription = null,
                              tint = TextLightGray,
                              modifier = Modifier.size(20.dp)
                          )
                      },
                      colors = OutlinedTextFieldDefaults.colors(
                          unfocusedBorderColor = BorderColor,
                          focusedBorderColor =  MaterialTheme.colorScheme.primary,
                          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                          focusedContainerColor = MaterialTheme.colorScheme.surface,
                          focusedTextColor = TextDark,
                          unfocusedTextColor = TextDark
                      )
                  )

                  // Espaciador flexible para empujar el botón a la parte inferior de la pantalla
                  Spacer(modifier = Modifier.weight(1f))

                  // 4. Botón Inferior
                  Button(
                      onClick = {
                          if (identifier.isNotBlank()) {
                              viewModel.sendOtp(identifier)
                          }
                      },
                      modifier = Modifier
                          .fillMaxWidth()
                          .height(70.dp)
                          .padding(bottom = 24.dp), // Margen inferior antes del borde de la pantalla
                      shape = RoundedCornerShape(12.dp),
                      enabled = identifier.isNotBlank(), // Se deshabilita si está vacío
                      colors = ButtonDefaults.buttonColors(
                          containerColor =  MaterialTheme.colorScheme.primary,
                          disabledContainerColor =  MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                      )
                  ) {
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.Center
                      ) {
                          Text(
                              text = "Enviar Código",
                              fontSize = 16.sp,
                              fontWeight = FontWeight.Bold,
                              color = Color.White
                          )
                          Spacer(modifier = Modifier.width(8.dp))
                          Icon(
                              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                              contentDescription = "Enviar",
                              tint = Color.White,
                              modifier = Modifier.size(18.dp)
                          )
                      }
                  }
              }

          }


      }
      LoadingOverlay(
          isLoading = isLoading,
          lottieRes = R.raw.loadings
      )
  }

}