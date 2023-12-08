package com.example.apitest.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.apitest.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import android.widget.Button

data class Berry(
    val name: String,
    val url: String
)

data class BerryInfo(
    val berry: Berry,
    val potency: Int
)

data class BerryFlavorResponse(
    val berries: List<BerryInfo>
)

interface ApiService {
    @GET("berry-flavor/{id}")
    fun getBerryFlavor(@Path("id") id: Int): Call<BerryFlavorResponse>
}

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var textView: TextView
    private lateinit var apiService: ApiService
    private var currentId = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        textView = binding.textHome
        val buttonNext: Button = binding.button
        val buttonPrevious: Button = binding.button2

        val retrofit = Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)

        buttonNext.setOnClickListener {
            // Incrementa el ID o reinicia si alcanza un límite específico
            currentId = (currentId % 10) + 1
            fetchData(currentId)
        }

        buttonPrevious.setOnClickListener {
            // Decrementa el ID o reinicia si es menor que 1
            currentId = if (currentId > 1) currentId - 1 else 10
            fetchData(currentId)
        }

        // Realiza la primera solicitud al cargar la vista
        fetchData(currentId)

        return root
    }

    private fun fetchData(id: Int) {
        val call = apiService.getBerryFlavor(id)

        call.enqueue(object : Callback<BerryFlavorResponse> {
            override fun onResponse(
                call: Call<BerryFlavorResponse>,
                response: Response<BerryFlavorResponse>
            ) {
                if (response.isSuccessful) {
                    val berries = response.body()?.berries
                    if (berries != null && berries.isNotEmpty()) {
                        val firstBerry = berries[0]
                        val berryName = firstBerry.berry.name
                        val berryPotency = firstBerry.potency

                        textView.text = "Baya: $berryName\nPotencia: $berryPotency"
                    } else {
                        textView.text = "No se encontraron bayas en la respuesta"
                    }
                } else {
                    // Manejar el error
                    textView.text = "Error en la solicitud"
                }
            }

            override fun onFailure(call: Call<BerryFlavorResponse>, t: Throwable) {
                // Manejar el error
                textView.text = "Error en la solicitud: ${t.message}"
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}