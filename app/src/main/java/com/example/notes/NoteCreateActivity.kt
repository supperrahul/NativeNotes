package com.example.notes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.note_create_layout.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.floor

class NoteCreateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.note_create_layout)
        supportActionBar?.subtitle = "save a note"
        window.navigationBarColor=getColor(R.color.orange)
        window.statusBarColor=getColor(R.color.orange)
        supportActionBar?.setBackgroundDrawable(getDrawable(R.color.orange))
        chip.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(Element: CompoundButton?, status: Boolean) {
                if (status) {
                    val pref =  getSharedPreferences(getString(R.string.PRIVACY_FILE), MODE_PRIVATE)
                    val code  = pref.getString(getString(R.string.PRIVACY_FILE),"DEFAULT_CODE")
                    if (code == "DEFAULT_CODE") {
                        // privacy passcode is not set
                        showdialog()
                    }

                }
            }

        })
    }

    private fun showdialog() {
        val pref = getSharedPreferences(getString(R.string.PRIVACY_FILE), MODE_PRIVATE)
        val dialog = AlertDialog.Builder(this)
        dialog.setCancelable(false)
        val et = EditText(this)

        dialog.setNegativeButton("later") { p1, p2 ->
            chip.isChecked = false
        }
        dialog.setPositiveButton("set passcode") { p1, p2 ->
            with(pref.edit()) {
                if(et.text.toString()!=""){
                    putString(getString(R.string.PRIVACY_FILE), et.text.toString())
                    Toast.makeText(applicationContext, "set as new passcode", Toast.LENGTH_SHORT).show()
                    apply()
                }else{
                    chip.isChecked=false
                    Toast.makeText(applicationContext, "Enter a Passcode first", Toast.LENGTH_SHORT).show()
                }
                    }

        }
        dialog.setView(et)
        dialog.setTitle("Set Privacy Password")
        dialog.setMessage("Please set your privacy password(you can not change this password in future!)")
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.title) {
            "save note" -> {
                try {
                    if ((titleOfNote.text.toString() != "") || (msgOfNote.text.toString() != "")) {

                        val date = Date()
                        val map = mutableMapOf<String, String>()
                        val jsonFile = File(cacheDir.path+"/jsonfile.json")
                        val jsonData = jsonFile.readText()
                        val data: ArrayList<Map<String, String>> = arrayListOf()

                        val fileName = getRandomNum() + ".txt"
                        val txtFile = File(cacheDir.path+"/" + fileName)
                        txtFile.createNewFile()
                        if (!jsonFile.exists()) {
                            jsonFile.createNewFile()
                            jsonFile.writeText("[]")
                        }
                        map["title"] = titleOfNote.text.toString()
                        map["create_time"] = "${date.date}/${date.month}/${1900 + date.year}"
                        map["file_name"] = fileName
                        map["private_mode"] = chip.isChecked.toString()
                        txtFile.writeText(msgOfNote.text.toString())


                        val jsonArray = JSONArray(jsonData)
                        var index = 0;
                        while (index < jsonArray.length()) {

                            val jonMap = JSONObject(jsonArray[index].toString())
                            data.add(
                                mapOf(
                                    "title" to jonMap.getString("title"),
                                    "create_time" to jonMap.getString("create_time"),
                                    "file_name" to jonMap.getString("file_name"),
                                    "private_mode" to jonMap.getString("private_mode")
                                )
                            )
                            index++
                        }

                        data.add(map)
                        jsonFile.writeText(JSONArray(data).toString())
                        Toast.makeText(this, "saved", Toast.LENGTH_SHORT).show()
                        finish()
                    } else
                        Toast.makeText(this, "please fill the fields", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getRandomNum(): String {
        var result = ""

        while (result.length < 6) {
            result += floor((Math.random() * 8) + 1).toInt()
        }



        return result

    }

    override fun onBackPressed() {
        if ((titleOfNote.text.toString() != "") || (msgOfNote.text.toString() != "")) {
            confirmExit()
        } else
            super.onBackPressed()

    }

    private fun confirmExit() {
        val dialog = AlertDialog.Builder(this)
        dialog.setNegativeButton("no") { p1, p2 ->

        }
        dialog.setPositiveButton("hmm") { p1, p2 ->
            finish()

        }

        dialog.setTitle("Dismiss Note?")
        dialog.setMessage("The note will not saved")
        dialog.show()
    }

}