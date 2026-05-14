package com.example.bebek1

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    // SharedPreferences Anahtarları
    private val PREFS_NAME = "BebekTakipciPrefs"
    private val KEY_LAST_FEEDING = "lastFeedingTime"
    private val KEY_LAST_SLEEP = "lastSleepTime"

    // Bebek Bilgileri Anahtarları (SettingsActivity'den okunacak)
    private val KEY_BEBEK_ADI = "bebekAdi"
    private val KEY_DOGUM_TARIHI = "dogumTarihi"
    private val KEY_BOY = "bebekBoy"
    private val KEY_KILO = "bebekKilo"

    // Arayüz (UI) Bileşenleri
    private lateinit var tvSonBeslenme: TextView
    private lateinit var tvSonUyku: TextView
    private lateinit var tvBeslenmeGecenSure: TextView
    private lateinit var tvUykuGecenSure: TextView

    // Bebek Bilgileri Alanındaki TextView'ler
    private lateinit var tvBebekAdi: TextView
    private lateinit var tvBebekYasi: TextView
    private lateinit var tvBebekBoyKilo: TextView

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. UI Bileşenlerini Bağlama

        // Bebek Bilgileri Alanı
        tvBebekAdi = findViewById(R.id.textView_bebekAdi)
        tvBebekYasi = findViewById(R.id.textView_bebekYasi)
        tvBebekBoyKilo = findViewById(R.id.textView_bebekBoyKilo)

        // Beslenme Alanı
        tvSonBeslenme = findViewById(R.id.textView_sonBeslenmeZamani)
        tvBeslenmeGecenSure = findViewById(R.id.textView_beslenmeGecenSure)

        // Uyku Alanı
        tvSonUyku = findViewById(R.id.textView_sonUykuZamani)
        tvUykuGecenSure = findViewById(R.id.textView_uykuGecenSure)

        val btnBeslenme = findViewById<Button>(R.id.button_beslenmeyiKaydet)
        val btnUyku = findViewById<Button>(R.id.button_uykuyuKaydet)

        // 2. SharedPreferences'ı Başlatma
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 3. Ayarlar ekranını açma olayını tanımla
        tvBebekAdi.setOnClickListener { openSettings() }

        // 4. Beslenme Düğmesi Olayı
        btnBeslenme.setOnClickListener {
            recordTime(KEY_LAST_FEEDING, tvSonBeslenme, tvBeslenmeGecenSure)
        }

        // 5. Uyku Düğmesi Olayı
        btnUyku.setOnClickListener {
            recordTime(KEY_LAST_SLEEP, tvSonUyku, tvUykuGecenSure)
        }
    }

    override fun onResume() {
        super.onResume()
        // Uygulama her ön plana geldiğinde (SettingsActivity'den dönülünce dahil) bilgileri yükle.
        updateBabyInfo()
        loadTimes()
    }

    // --- Yardımcı Fonksiyonlar ---

    private fun updateBabyInfo() {
        val bebekAdi = prefs.getString(KEY_BEBEK_ADI, "Minik Bebek")
        val dogumTarihiMillis = prefs.getLong(KEY_DOGUM_TARIHI, 0L)
        val boy = prefs.getString(KEY_BOY, "--")
        val kilo = prefs.getString(KEY_KILO, "--")

        // UI Güncelleme
        tvBebekAdi.text = "👶 $bebekAdi (Ayarlamak için tıkla)"
        tvBebekYasi.text = calculateAge(dogumTarihiMillis)
        tvBebekBoyKilo.text = "Boy: $boy cm | Kilo: $kilo kg"
    }

    private fun openSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun calculateAge(dogumTarihiMillis: Long): String {
        if (dogumTarihiMillis == 0L) {
            return "Yaş Verisi Yok (Ayarlayın)"
        }

        val dogumTarihi = Calendar.getInstance().apply { timeInMillis = dogumTarihiMillis }
        val bugun = Calendar.getInstance()

        val diffMillis = bugun.timeInMillis - dogumTarihi.timeInMillis
        val days = diffMillis / (1000 * 60 * 60 * 24)

        val weeks = days / 7
        val remainingDays = days % 7

        return when {
            days > 90 -> "${(days / 30)} aylık, ${weeks} haftalık"
            weeks > 0 -> "${weeks} haftalık, ${remainingDays} günlük"
            else -> "${days} günlük"
        }
    }

    private fun recordTime(key: String, timeTextView: TextView, diffTextView: TextView) {
        val currentTimeMillis = System.currentTimeMillis()

        prefs.edit().apply {
            putLong(key, currentTimeMillis)
            apply()
        }
        updateUI(currentTimeMillis, timeTextView, diffTextView)
    }

    private fun loadTimes() {
        val lastFeedingTime = prefs.getLong(KEY_LAST_FEEDING, 0L)
        val lastSleepTime = prefs.getLong(KEY_LAST_SLEEP, 0L)

        if (lastFeedingTime != 0L) {
            updateUI(lastFeedingTime, tvSonBeslenme, tvBeslenmeGecenSure)
        } else {
            tvSonBeslenme.text = "Kaydedilen Zaman: Veri Yok"
            tvBeslenmeGecenSure.text = "Kayıt Başlatılmadı"
        }

        if (lastSleepTime != 0L) {
            updateUI(lastSleepTime, tvSonUyku, tvUykuGecenSure)
        } else {
            tvSonUyku.text = "Kaydedilen Zaman: Veri Yok"
            tvUykuGecenSure.text = "Kayıt Başlatılmadı"
        }
    }

    private fun updateUI(timeMillis: Long, timeTextView: TextView, diffTextView: TextView) {
        val formatter = SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault())
        timeTextView.text = "Kaydedilen Zaman: ${formatter.format(Date(timeMillis))}"
        diffTextView.text = calculateTimeDifference(timeMillis)
    }

    private fun calculateTimeDifference(targetTimeMillis: Long): String {
        if (targetTimeMillis == 0L) {
            return "Kayıt Yok"
        }

        val now = System.currentTimeMillis()
        val difference = now - targetTimeMillis

        val seconds = difference / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days gün önce"
            hours > 0 -> "$hours saat ${minutes % 60} dakika önce"
            minutes > 0 -> "$minutes dakika önce"
            else -> "Şimdi"
        }
    }
}