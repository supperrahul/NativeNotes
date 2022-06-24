package com.example.notes

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings

import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import androidx.core.view.forEach

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_layout.view.*

import org.json.JSONArray
import org.json.JSONObject
import java.io.File


class MainActivity : AppCompatActivity() {

    private var items = mutableListOf<View>()
    lateinit var jsonFile:File
    private var selectedItems = arrayListOf<Int>()
    private var mode :Int =Modes.NORMAL
    private var current_menu  = R.menu.main_menu
    private var notesList = arrayListOf<Map<String,String>>()
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
          jsonFile = File(cacheDir.path+"/jsonfile.json")
        setContentView(R.layout.activity_main)
        window.navigationBarColor=getColor(R.color.crimson)
        window.statusBarColor=getColor(R.color.crimson)
        supportActionBar?.setBackgroundDrawable(getDrawable(R.color.crimson))
        supportActionBar?.title="Native Notes"
    }


    private fun readJsonAndShow() {

        try {
            if(!jsonFile.exists()){
                jsonFile.createNewFile()
                jsonFile.writeText("[]")
            }

            val jsonData = jsonFile.readText()
            val jsonArray = JSONArray(jsonData)
            var index = 0
            items= mutableListOf()
            noteListW.removeAllViews()
            notesList= arrayListOf()
            supportActionBar?.subtitle="Total Notes: ${jsonArray.length()}"
            if(jsonArray.length()>0)
                while (index < jsonArray.length()) {

                    val map = JSONObject(jsonArray[index].toString())
                    val detail =  mapOf("title" to map.getString("title"),
                        "create_time" to map.getString("create_time"),
                        "file_name" to map.getString("file_name"),
                        "private_mode" to map.getString("private_mode"))
                    showListOfNotes(detail,index)
                    notesList.add(detail)

                    index++
                }

        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(current_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.create->{
                val intent = Intent(this,NoteCreateActivity().javaClass)
                val options = ActivityOptions.makeCustomAnimation(this,android.R.anim.fade_in,android.R.anim.fade_out)
                startActivity(intent,options.toBundle())
            }
            R.id.delete->{
                for (selectedItem in selectedItems) {
                    try{
                        val targetFile = File(cacheDir.path+"/"+notesList[selectedItem]["file_name"]!!)
                        if (targetFile.exists()) targetFile.delete()
                        notesList.removeAt(selectedItem)
                        jsonFile.writeText(JSONArray(notesList).toString())
                        noteListW.removeViewAt(selectedItem)

                    }catch (e:Exception){
                        Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
                current_menu  = R.menu.main_menu
                invalidateOptionsMenu()
                mode=Modes.NORMAL
                readJsonAndShow()
            }

            R.id.OIS->{
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package",packageName,null)
                intent.data = uri
                startActivity(
                    intent
                )
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showListOfNotes(map:Map<String, String>,index:Int) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.item_layout,null)
        view.title.text=map["title"]
        view.time.text=map["create_time"]
        view.tag=index
        val txtFile = File(cacheDir.path+"/"+map["file_name"])
        items.add(view)
        if(map["private_mode"]=="true"){
            "click to read".also { view.lines.text = it }
        }else{
            if(txtFile.readText().length>20){
                view.lines.text=pad(txtFile.readText())
            }else{
                view.lines.text=txtFile.readText()
            }

        }
        view.setOnClickListener {
         clickFunction(it)
        }
        view.setOnLongClickListener {
            longClickFunction(it)
            true
        }
        try {
            noteListW.addView(view)
        }
        catch (e:Exception){
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
        }
    }


    override fun onStart() {

            if(mode==Modes.NORMAL)
                  readJsonAndShow()
        super.onStart()
    }

    private fun pad(str:String,length:Int=30):String{
        return str.substring(0,length)+"..."
    }
    private fun clickFunction(view:View){

        val index = getInt(view.tag)
    if(notesList[index]["private_mode"]=="true"){
       askPassCodeAndSwitch(index)
    }else{
        val intent = Intent(applicationContext,SeeEditActivity().javaClass)
            .putExtra("index",index)
            .putExtra("details", notesList)
        startActivity(intent)

    }

    }

    private fun askPassCodeAndSwitch(index:Int) {
        val pref =  getSharedPreferences(getString(R.string.PRIVACY_FILE), MODE_PRIVATE)

        val dialog = AlertDialog.Builder(this)
        val et = EditText(this)
        et.hint="Enter Your PassCode"

        dialog.setPositiveButton("verify"){
                p1,p2->

            if(pref.getString(getString(R.string.PRIVACY_FILE),"DEFAULT_CODE")==et.text.toString()){
                val intent = Intent(applicationContext,SeeEditActivity().javaClass)
                    .putExtra("index",index)
                    .putExtra("details", notesList)
                startActivity(intent)
            }else
                Toast.makeText(this, "please try again!", Toast.LENGTH_SHORT).show()

        }

        Handler(Looper.myLooper()!!).postDelayed({
            et.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(et,InputMethodManager.SHOW_IMPLICIT)
        },500)
        dialog.setView(et)
        dialog.setTitle("Enter Privacy Password")
        dialog.setMessage("Please enter your privacy password")
        dialog.show()

    }

    private fun longClickFunction(view:View){
       if(mode==Modes.NORMAL){
           mode=Modes.SELECT
           clickSelected(view)
           noteListW.forEach {
               it.setOnClickListener {
                   clickSelected(it)
               }
           }
           current_menu=R.menu.selected_menu
           invalidateOptionsMenu()
       }
//        Toast.makeText(this, "selected "+view.title.text, Toast.LENGTH_SHORT).show()
    }
    private fun setSelected(view: View){
        selectedItems.add(getInt(view.tag))
        view.background=getDrawable(R.color.green)
//        Toast.makeText(applicationContext, selectedItems.toString(), Toast.LENGTH_SHORT).show()
    }
    private fun clickSelected(view: View){

        if (selectedItems.contains(getInt(view.tag))) {
            unSelect(view)
        } else {
            setSelected(view)
        }
    }

    private fun unSelect(view: View) {
//        Toast.makeText(applicationContext, "unselected", Toast.LENGTH_SHORT).show()
        selectedItems.remove(view.tag)
        view.background=getDrawable(R.color.white)
    }

    private object Modes{
        const val NORMAL = 1
        const val SELECT = 2
    }

    override fun onBackPressed() {
        if(mode==Modes.SELECT){
            mode=Modes.NORMAL
            current_menu=R.menu.main_menu
            invalidateOptionsMenu()
            selectedItems= arrayListOf()
            noteListW.forEach {
                it.background=getDrawable(R.color.white)
                it.setOnClickListener {
                    clickFunction(it)
                }
            }
        }else
         super.onBackPressed()
    }

    private  fun getInt(x:Any)=x.toString().toInt()


}