/*
 * Leitor de Acelerômetro (eixos X, Y, Z, aceleração linear e gravidade)
 *
 * Aluno: Samuel Araujo
 * RA: 210025640
 * Curso: Engenharia da Computação
 * Instituição: UNISAL - Campus Campinas, Unidade São José
 */
package br.com.unisal.samuelaraujo.acelerometro

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.com.unisal.samuelaraujo.acelerometro.ui.theme.AcelerometroTheme
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sqrt

class MainActivity : ComponentActivity(), SensorEventListener {

    // SensorManager e as 3 referencias de sensor usadas nesta tela. Podem ficar
    // null se o aparelho nao tiver o sensor correspondente - por isso o "?"
    // e os tratamentos de sensor ausente na interface (ver criaTelaAcelerometro).
    private lateinit var sensorManager: SensorManager
    private var sensorAcelerometro: Sensor? = null
    private var sensorAceleracaoLinear: Sensor? = null
    private var sensorGravidade: Sensor? = null

    // Valores dos eixos do acelerometro (inclui o efeito da gravidade), em m/s^2
    var eixoX by mutableStateOf(0f)
    var eixoY by mutableStateOf(0f)
    var eixoZ by mutableStateOf(0f)

    // Modulo (magnitude) da aceleracao linear do dispositivo, em m/s^2
    var aceleracaoLinear by mutableStateOf(0f)

    // Modulo (magnitude) da aceleracao da gravidade, em m/s^2
    var aceleracaoGravidade by mutableStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorAcelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorAceleracaoLinear = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        sensorGravidade = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        setContent {
            AcelerometroTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    criaTelaAcelerometro(innerPadding)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Registra os listeners apenas enquanto a tela esta visivel, para economizar bateria.
        sensorAcelerometro?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        sensorAceleracaoLinear?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        sensorGravidade?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                eixoX = event.values[0]
                eixoY = event.values[1]
                eixoZ = event.values[2]
            }

            Sensor.TYPE_LINEAR_ACCELERATION -> {
                aceleracaoLinear = modulo(event.values)
            }

            Sensor.TYPE_GRAVITY -> {
                aceleracaoGravidade = modulo(event.values)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Nao utilizado nesta tela.
    }

    /**
     * Calcula o modulo (magnitude) de um vetor de 3 eixos: sqrt(x^2 + y^2 + z^2).
     */
    private fun modulo(valores: FloatArray): Float {
        return sqrt(valores[0] * valores[0] + valores[1] * valores[1] + valores[2] * valores[2])
    }

    /**
     * Normaliza um valor de aceleracao (aproximadamente -20..20 m/s^2) para a faixa 0..1,
     * usada apenas para dar um feedback visual proporcional a intensidade do movimento.
     */
    private fun normalizar(valor: Float): Float {
        val maximo = 20f
        return min(abs(valor) / maximo, 1f)
    }

    /**
     * Tela principal: cabecalho, card dos 3 eixos e os dois cards de valor unico
     * (aceleracao linear e gravidade). Rola verticalmente para caber em telas menores.
     */
    @Composable
    fun criaTelaAcelerometro(innerPadding: PaddingValues) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column {
                Text(
                    text = "Acelerômetro",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Leitura em tempo real dos sensores de movimento do aparelho",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (sensorAcelerometro == null) {
                CardAviso("Este aparelho não possui sensor de acelerômetro.")
            }

            CardEixos(eixoX, eixoY, eixoZ)

            CardValorUnico(
                titulo = "Aceleração Linear",
                descricao = "Aceleração do dispositivo sem o efeito da gravidade",
                valor = aceleracaoLinear,
                corDestaque = MaterialTheme.colorScheme.tertiary,
                disponivelNoAparelho = sensorAceleracaoLinear != null
            )

            CardValorUnico(
                titulo = "Aceleração da Gravidade",
                descricao = "Componente da gravidade detectada pelo aparelho",
                valor = aceleracaoGravidade,
                corDestaque = MaterialTheme.colorScheme.secondary,
                disponivelNoAparelho = sensorGravidade != null
            )
        }
    }

    /** Aviso exibido quando um sensor necessario nao existe no aparelho. */
    @Composable
    fun CardAviso(mensagem: String) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Text(
                text = mensagem,
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }

    /** Card com os 3 eixos do acelerometro bruto, cada um em uma LinhaEixo. */
    @Composable
    fun CardEixos(x: Float, y: Float, z: Float) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Eixos do Acelerômetro",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Aceleração bruta captada em cada eixo, incluindo a gravidade",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider()
                LinhaEixo("X", x, Color(0xFFE53935))
                LinhaEixo("Y", y, Color(0xFF43A047))
                LinhaEixo("Z", z, Color(0xFF1E88E5))
            }
        }
    }

    /**
     * Uma linha de eixo: bolinha colorida + rotulo + barra de progresso (feedback
     * visual proporcional a intensidade, via normalizar()) + valor numerico em m/s^2.
     */
    @Composable
    fun LinhaEixo(nome: String, valor: Float, cor: Color) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(cor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Eixo $nome",
                modifier = Modifier.width(66.dp),
                fontWeight = FontWeight.Medium
            )
            LinearProgressIndicator(
                progress = { normalizar(valor) },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = cor,
                trackColor = cor.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "%.2f m/s²".format(valor),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(96.dp)
            )
        }
    }

    /**
     * Card para um valor unico em destaque (usado para aceleracao linear e
     * gravidade): titulo, descricao, e o valor em m/s^2 na cor de destaque.
     * Mostra um aviso se o sensor correspondente nao existir no aparelho.
     */
    @Composable
    fun CardValorUnico(
        titulo: String,
        descricao: String,
        valor: Float,
        corDestaque: Color,
        disponivelNoAparelho: Boolean
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = titulo, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = descricao,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!disponivelNoAparelho) {
                        Text(
                            text = "Sensor indisponível neste aparelho",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "%.2f".format(valor),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = corDestaque
                    )
                    Text(
                        text = " m/s²",
                        fontSize = 14.sp,
                        color = corDestaque,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
            }
        }
    }
}
