package com.example.content_providers

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import androidx.core.view.GravityCompat
import androidx.appcompat.app.ActionBarDrawerToggle
import android.view.MenuItem
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.view.Menu
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    public fun addRecord(view: View) {
        var values = ContentValues()
        if (!(name.text.toString().isEmpty())
            && (!(nickname.text.toString().isEmpty()))) {
            values.put(CustomContentpRroviders.NAME, name.text.toString())
            values.put(CustomContentpRroviders.NICK_NAME,nickname.text.toString())
            contentResolver.insert(CustomContentpRroviders.CONTENT_URI,values)
            Toast.makeText(baseContext, "Record Inserted",Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(baseContext, "Please enter the records first",Toast.LENGTH_LONG).show()
        }
    }

    public fun showAllRecords(view: View) {
        // Show all the records sorted by friend’s name
        val URL = "content://com.androidatc.provider/nicknames"
        var friends = Uri.parse(URL)
        var c = contentResolver.query(friends, null, null, null, "name")
        var result = "Content Provider Results:"
        if (c != null) {
            if (!c.moveToFirst()) {
                Toast.makeText(this, result + " no content yet!", Toast.LENGTH_LONG).show()
            } else {
                do {
                    result += "\n${c?.getString(c.getColumnIndex(CustomContentpRroviders.NAME))} with id ${c.getString(c.getColumnIndex(CustomContentpRroviders.ID))} has nickname: ${c.getString(c.getColumnIndex(CustomContentpRroviders.NICK_NAME))}"
                } while (c!!.moveToNext())
                if (!result.isEmpty())
                    Toast.makeText(this, result, Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(this, "No Records present", Toast.LENGTH_LONG).show()
            }
        }
    }

    public fun deleteAllRecords(view: View) {
        //delete all the records and the table of the database provider
        val URL = "content://com.androidatc.provider/nicknames"
        var friends = Uri.parse(URL)
        var count = contentResolver.delete(friends, null, null)
        var countNum = "$count records are deleted."
        Toast.makeText(baseContext, countNum, Toast.LENGTH_LONG).show()
    }



}
