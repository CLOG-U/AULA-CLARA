package ec.edu.aulaclara.ui

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun SplashScreen() {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        BrandMark(96)
        Spacer(Modifier.height(24.dp))
        Text("Aula Clara", style = MaterialTheme.typography.headlineLarge)
        Text("Tu próximo paso, a tiempo.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(48.dp))
        LinearProgressIndicator(Modifier.width(160.dp))
        Spacer(Modifier.height(12.dp))
        Text("Preparando tu agenda", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AuthScreen(register: Boolean, onSwitch: () -> Unit, onEnter: (String, String, Boolean) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // Passwords deliberately stay out of saved state and the demonstration's session.
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    val emailError = attempted && !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val passwordError = attempted && password.length < 8
    val nameError = attempted && register && name.isBlank()
    val confirmationError = attempted && register && password != confirmation
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 520.dp).fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (register) IconButton(onSwitch) { Icon(AulaIcons.ArrowBack, "Volver al acceso") }
                BrandMark()
            }
            PageTitle(if (register) "Crea tu cuenta" else "Qué bueno verte", if (register) "Empieza con lo esencial." else "Entra y organiza tus próximas entregas.")
            if (register) OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth().testTag("register_name"),
                label = { Text("Nombre") }, singleLine = true, isError = nameError,
                supportingText = if (nameError) ({ Text("Escribe tu nombre para continuar.") }) else null)
            OutlinedTextField(email, { email = it }, Modifier.fillMaxWidth().testTag("auth_email"), label = { Text("Correo electrónico") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true, isError = emailError,
                supportingText = if (emailError) ({ Text("Escribe un correo válido, por ejemplo nombre@correo.com.") }) else null)
            OutlinedTextField(password, { password = it }, Modifier.fillMaxWidth().testTag("auth_password"), label = { Text("Contraseña") },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), singleLine = true, isError = passwordError,
                trailingIcon = { IconButton({ showPassword = !showPassword }) { Icon(if (showPassword) AulaIcons.VisibilityOff else AulaIcons.Visibility, if (showPassword) "Ocultar contraseña" else "Mostrar contraseña") } },
                supportingText = { Text(if (passwordError) "La contraseña debe tener al menos 8 caracteres." else "Usa al menos 8 caracteres.") })
            if (register) OutlinedTextField(confirmation, { confirmation = it }, Modifier.fillMaxWidth(), label = { Text("Confirmar contraseña") },
                visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true, isError = confirmationError,
                supportingText = if (confirmationError) ({ Text("Las contraseñas deben coincidir.") }) else null)
            PrimaryButton(if (register) "Crear cuenta" else "Iniciar sesión", {
                attempted = true
                if (Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() && password.length >= 8 && (!register || (name.isNotBlank() && password == confirmation))) {
                    onEnter(if (register) name else email.substringBefore('@'), email, false)
                }
            }, Modifier.testTag("auth_submit"))
            TextButton(onSwitch, Modifier.align(Alignment.CenterHorizontally)) { Text(if (register) "Ya tengo cuenta" else "Crear cuenta") }
            InfoBox("MVP académico: el acceso es demostrativo. No se verifican cuentas reales ni se guardan contraseñas. La agenda se reinicia al cerrar sesión.")
            if (!register) SecondaryButton("Explorar demostración", { onEnter("Carlos Ortiz", "carlos@ejemplo.com", true) }, Modifier.testTag("login_demo"))
        }
    }
}
