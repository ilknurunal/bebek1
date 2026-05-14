package com.example.bebek1

// GEREKLİ TÜM ANDROID VE KOTLIN KÜTÜPHANELERİ BURADA TOPLANDI
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View // Toggle (VISIBLE/GONE) için
import android.widget.ArrayAdapter // Spinner için
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout // LinearLayout bileşeni için
import android.widget.Spinner // Spinner bileşeni için
import android.widget.TextView // TextView bileşeni için
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    // UI Bileşenleri (Temel ve Fiziksel)
    private lateinit var editTextBebekAdi: EditText
    private lateinit var buttonDogumTarihiSec: Button
    private lateinit var editTextBoy: EditText
    private lateinit var editTextKilo: EditText

    // UI Bileşenleri (Sağlık Detayları)
    private lateinit var spinnerKanGrubu: Spinner
    private lateinit var editTextHastalik: EditText
    private lateinit var editTextAlerji: EditText
    private lateinit var layoutSaglikDetaylari: LinearLayout
    private lateinit var textViewSaglikBaslik: TextView

    // UI Bileşenleri (Aşı/İlaç)
    private lateinit var editTextAsiAdi: EditText
    private lateinit var buttonAsiTarihiSec: Button
    private lateinit var editTextAsiDozu: EditText
    private lateinit var editTextIlacAdi: EditText
    private lateinit var buttonIlacTarihiSec: Button
    private lateinit var editTextIlacDozu: EditText
    private lateinit var layoutAsiIlacDetaylari: LinearLayout
    private lateinit var textViewAsiIacBaslik: TextView

    // Veri (Zaman damgaları)
    private var dogumTarihiMillis: Long = 0L
    private var asiTarihiMillis: Long = 0L
    private var ilacTarihiMillis: Long = 0L

    // Sabitler (SharedPreferences Anahtarları)
    private val PREFS_NAME = "BebekTakipciPrefs"
    private val KEY_BEBEK_ADI = "bebekAdi"
    private val KEY_DOGUM_TARIHI = "dogumTarihi"
    private val KEY_BOY = "bebekBoy"
    private val KEY_KILO = "bebekKilo"
    private val KEY_KAN_GRUBU = "kanGrubu"
    private val KEY_HASTALIK = "hastaliklar"
    private val KEY_ALERJI = "alerjiler"
    private val KEY_ASI_ADI = "asiAdi"
    private val KEY_ASI_TARIHI = "asiTarihi"
    private val KEY_ASI_DOZU = "asiDozu"
    private val KEY_ILAC_ADI = "ilacAdi"
    private val KEY_ILAC_TARIHI = "ilacTarihi"
    private val KEY_ILAC_DOZU = "ilacDozu"

    private val KAN_GRUPLARI = arrayOf("Seçiniz", "A Rh+", "A Rh-", "B Rh+", "B Rh-", "AB Rh+", "AB Rh-", "0 Rh+", "0 Rh-")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 1. **ÖNCELİKLE** TÜM UI BİLEŞENLERİNİ BAĞLA (findViewById)
        // Temel Bilgiler
        editTextBebekAdi = findViewById(R.id.editText_bebekAdi)
        buttonDogumTarihiSec = findViewById(R.id.button_dogumTarihiSec)
        val buttonAyarlariKaydet: Button = findViewById(R.id.button_ayarlariKaydet)

        // Fiziksel Veriler
        editTextBoy = findViewById(R.id.editText_bebekBoy)
        editTextKilo = findViewById(R.id.editText_bebekKilo)

        // Sağlık Paneli
        textViewSaglikBaslik = findViewById(R.id.textView_saglikBaslik)
        layoutSaglikDetaylari = findViewById(R.id.layout_saglikDetaylari)
        spinnerKanGrubu = findViewById(R.id.spinner_kanGrubu)
        editTextHastalik = findViewById(R.id.editText_hastalik)
        editTextAlerji = findViewById(R.id.editText_alerji)

        // Aşı/İlaç Paneli
        textViewAsiIacBaslik = findViewById(R.id.textView_asiIacBaslik)
        layoutAsiIlacDetaylari = findViewById(R.id.layout_asiIacDetaylari)
        editTextAsiAdi = findViewById(R.id.editText_asiAdi)
        buttonAsiTarihiSec = findViewById(R.id.button_asiTarihiSec)
        editTextAsiDozu = findViewById(R.id.editText_asiDozu)
        editTextIlacAdi = findViewById(R.id.editText_ilacAdi)
        buttonIlacTarihiSec = findViewById(R.id.button_ilacTarihiSec)
        editTextIlacDozu = findViewById(R.id.editText_ilacDozu)


        // Spinner'ı doldurma mantığı
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, KAN_GRUPLARI)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKanGrubu.adapter = adapter

        // 2. ARTIK GÜVENLİ: Mevcut verileri yükle
        loadSettings()

        // 3. Olay Dinleyicilerini Kur
        buttonDogumTarihiSec.setOnClickListener { showDatePicker("dogum") }
        buttonAyarlariKaydet.setOnClickListener { saveSettings() }

        // Açılır Kapanır Olaylar
        textViewSaglikBaslik.setOnClickListener { toggleSaglikDetaylari() }
        textViewAsiIacBaslik.setOnClickListener { toggleAsiIlacDetaylari() }
        buttonAsiTarihiSec.setOnClickListener { showDatePicker("asi") }
        buttonIlacTarihiSec.setOnClickListener { showDatePicker("ilac") }
    }

    // --- Yardımcı Fonksiyonlar ---

    private fun showDatePicker(tip: String) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Tarihi Seçin")
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateString = format.format(Date(selection))

            when (tip) {
                "dogum" -> {
                    dogumTarihiMillis = selection
                    buttonDogumTarihiSec.text = "📅 Doğum Tarihini Seç: $dateString"
                }
                "asi" -> {
                    asiTarihiMillis = selection
                    buttonAsiTarihiSec.text = "📅 Aşı Tarihini Seç: $dateString"
                }
                "ilac" -> {
                    ilacTarihiMillis = selection
                    buttonIlacTarihiSec.text = "📅 İlaç Tarihini Seç: $dateString"
                }
            }
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun loadSettings() {
        val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Temel Veriler
        editTextBebekAdi.setText(prefs.getString(KEY_BEBEK_ADI, ""))
        dogumTarihiMillis = prefs.getLong(KEY_DOGUM_TARIHI, 0L)
        if (dogumTarihiMillis != 0L) {
            buttonDogumTarihiSec.text = "📅 Doğum Tarihini Seç: ${format.format(Date(dogumTarihiMillis))}"
        }

        // Fiziksel Veriler
        editTextBoy.setText(prefs.getString(KEY_BOY, ""))
        editTextKilo.setText(prefs.getString(KEY_KILO, ""))

        // Sağlık Verileri
        val savedKanGrubu = prefs.getString(KEY_KAN_GRUBU, "")
        val kanGrubuIndex = KAN_GRUPLARI.indexOf(savedKanGrubu)
        if (kanGrubuIndex >= 0) {
            spinnerKanGrubu.setSelection(kanGrubuIndex)
        }
        editTextHastalik.setText(prefs.getString(KEY_HASTALIK, ""))
        editTextAlerji.setText(prefs.getString(KEY_ALERJI, ""))

        // Aşı ve İlaç Verileri
        editTextAsiAdi.setText(prefs.getString(KEY_ASI_ADI, ""))
        asiTarihiMillis = prefs.getLong(KEY_ASI_TARIHI, 0L)
        if (asiTarihiMillis != 0L) {
            buttonAsiTarihiSec.text = "📅 Aşı Tarihini Seç: ${format.format(Date(asiTarihiMillis))}"
        }
        editTextAsiDozu.setText(prefs.getString(KEY_ASI_DOZU, ""))

        editTextIlacAdi.setText(prefs.getString(KEY_ILAC_ADI, ""))
        ilacTarihiMillis = prefs.getLong(KEY_ILAC_TARIHI, 0L)
        if (ilacTarihiMillis != 0L) {
            buttonIlacTarihiSec.text = "📅 İlaç Tarihini Seç: ${format.format(Date(ilacTarihiMillis))}"
        }
        editTextIlacDozu.setText(prefs.getString(KEY_ILAC_DOZU, ""))
    }

    private fun saveSettings() {
        val bebekAdi = editTextBebekAdi.text.toString().trim()

        if (bebekAdi.isEmpty() || dogumTarihiMillis == 0L) {
            Toast.makeText(this, "Lütfen Bebek Adını ve Doğum Tarihini girin.", Toast.LENGTH_LONG).show()
            return
        }

        val kanGrubu = spinnerKanGrubu.selectedItem.toString()

        prefs.edit().apply {
            // Temel Veriler
            putString(KEY_BEBEK_ADI, bebekAdi)
            putLong(KEY_DOGUM_TARIHI, dogumTarihiMillis)

            // Fiziksel Veriler
            putString(KEY_BOY, editTextBoy.text.toString().trim())
            putString(KEY_KILO, editTextKilo.text.toString().trim())

            // Sağlık Detayları
            putString(KEY_KAN_GRUBU, if (kanGrubu == "Seçiniz") "" else kanGrubu)
            putString(KEY_HASTALIK, editTextHastalik.text.toString().trim())
            putString(KEY_ALERJI, editTextAlerji.text.toString().trim())

            // Aşı/İlaç Geçmişi
            putString(KEY_ASI_ADI, editTextAsiAdi.text.toString().trim())
            putLong(KEY_ASI_TARIHI, asiTarihiMillis)
            putString(KEY_ASI_DOZU, editTextAsiDozu.text.toString().trim())

            putString(KEY_ILAC_ADI, editTextIlacAdi.text.toString().trim())
            putLong(KEY_ILAC_TARIHI, ilacTarihiMillis)
            putString(KEY_ILAC_DOZU, editTextIlacDozu.text.toString().trim())

            apply()
        }

        Toast.makeText(this, "$bebekAdi bilgileri kaydedildi!", Toast.LENGTH_SHORT).show()
        finish()
    }

    // Sağlık Paneli Toggle Fonksiyonu
    private fun toggleSaglikDetaylari() {
        if (layoutSaglikDetaylari.visibility == View.GONE) {
            layoutSaglikDetaylari.visibility = View.VISIBLE
            textViewSaglikBaslik.text = "▲ Sağlık Bilgileri (Opsiyonel)"
        } else {
            layoutSaglikDetaylari.visibility = View.GONE
            textViewSaglikBaslik.text = "▼ Sağlık Bilgileri (Opsiyonel)"
        }
    }

    // Aşı/İlaç Paneli Toggle Fonksiyonu
    private fun toggleAsiIlacDetaylari() {
        if (layoutAsiIlacDetaylari.visibility == View.GONE) {
            layoutAsiIlacDetaylari.visibility = View.VISIBLE
            textViewAsiIacBaslik.text = "▲ Aşı ve İlaç Geçmişi (Son Kayıt)"
        } else {
            layoutAsiIlacDetaylari.visibility = View.GONE
            textViewAsiIacBaslik.text = "▼ Aşı ve İlaç Geçmişi (Son Kayıt)"
        }
    }
}