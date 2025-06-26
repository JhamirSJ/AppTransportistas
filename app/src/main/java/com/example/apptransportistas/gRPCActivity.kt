package com.example.apptransportistas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.apptransportistas.databinding.ActivityGrpcBinding
import io.grpc.ManagedChannelBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import saludarpb.HelloRequest
import saludarpb.SaludarServiceGrpc

class gRPCActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGrpcBinding
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGrpcBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEnviar.setOnClickListener {
            val name = binding.etNombreIngresado.text.toString()

            coroutineScope.launch {
                val response = Saludar(name)
                withContext(Dispatchers.Main) {
                    binding.tvRespuesta.text = response
                }
            }
        }
    }

    private fun Saludar(name: String): String {
        val channel = ManagedChannelBuilder.forAddress("192.168.10.159", 50051)
            .usePlaintext()
            .build()

        val stub = SaludarServiceGrpc.newBlockingStub(channel)

        val request = HelloRequest.newBuilder()
            .setName(name)
            .build()

        val response = stub.sayHello(request)
        channel.shutdown()
        return response.message
    }
}