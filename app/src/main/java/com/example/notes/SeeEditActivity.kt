package com.example.notes


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.see_edit_layout.*
import org.json.JSONArray

import java.io.File
import kotlin.properties.Delegates


class SeeEditActivity:AppCompatActivity() {
    var isChanged = false
    lateinit var details:ArrayList<Map<String,String>>
     var index by Delegates.notNull<Int>()
    lateinit var jsonFile:File
    lateinit var detail:Map<String,String>
    lateinit var  txtFile:File
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.see_edit_layout)
        jsonFile=File(cacheDir.path+"/jsonfile.json")
         details = intent?.extras?.get("details") as ArrayList<Map<String,String>>
         index = intent?.extras?.get("index") as Int
         detail = details[index]
        window.navigationBarColor=getColor(R.color.yellow)
        window.statusBarColor=getColor(R.color.yellow)
        supportActionBar?.setBackgroundDrawable(getDrawable(R.color.yellow))
        supportActionBar?.subtitle="edit note"
        editTitleOfNote.setText(detail["title"])
       txtFile  = File(cacheDir.path+"/"+detail["file_name"])
        if(txtFile.exists())
            editMsgOfNote.setText(txtFile.readText())
        editChip.isChecked=detail["private_mode"]=="true"

        editTitleOfNote.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int){}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
               changed()
            }

            override fun afterTextChanged(p0: Editable?){}

        })
        editMsgOfNote.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
               changed()
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        editChip.setOnCheckedChangeListener{
            p1,p2->
            changed()
        }




    }

    fun changed(){
        isChanged=true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.title){
            "save"->{
               save()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private  fun save(){
        details.removeAt(index)
        details.add(index,
            mapOf("title" to editTitleOfNote.text.toString(),
                "create_time" to detail["create_time"]!!,
                "file_name" to detail["file_name"]!!,
                "private_mode" to editChip.isChecked.toString()
            )
        )

        txtFile.writeText(editMsgOfNote.text.toString())
        jsonFile.writeText(JSONArray(details).toString())
        finish()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.see_edit_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
        if(!jsonFile.exists()){

            finish()
        }

        super.onStart()
    }

    override fun onBackPressed() {

     if(isChanged){
         val dialog = AlertDialog.Builder(this)

         dialog.setNegativeButton("exit"){
                 p1,p2->

             finish()
         }
         dialog.setPositiveButton("save and exit"){
                 p1,p2->
             save()
             finish()
         }
         dialog.setTitle("Leaving Without Save!")
         dialog.setMessage("save or dismiss?")
         dialog.show()
     }else
        super.onBackPressed()
    }
}