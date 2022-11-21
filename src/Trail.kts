// This section for DATABASE

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sandipbhattacharya.spaceshooter.GameOver
import com.sandipbhattacharya.spaceshooter.MainActivity
import com.sandipbhattacharya.spaceshooter.R
import com.sandipbhattacharya.spaceshooter.StartUp
import java.util.*

class Enemyarmy(var context: Context) {
    var enemyarmy: Bitmap
    var ex: Int
    var ey: Int
    var enemyVelocity: Int
    var random: Random

    init {
        enemyarmy = BitmapFactory.decodeResource(context.resources, R.drawable.teamplayer2)
        random = Random()
        ex = 200 + random.nextInt(400)
        ey = 0
        enemyVelocity = 14 + random.nextInt(10)
    }

    val enemySpaceshipWidth: Int
        get() = enemyarmy.width
    val enemySpaceshipHeight: Int
        get() = enemyarmy.height
}

// more help for the DATABASE


import android.database.sqlite.SQLiteOpenHelper
        import android.database.sqlite.SQLiteDatabase
        import android.content.ContentValues
        import android.content.Context
        import android.database.Cursor

class DBHelper(context: Context?) : SQLiteOpenHelper(context, "Userdata.db", null, 1) {
    override fun onCreate(DB: SQLiteDatabase) {
        DB.execSQL("create Table Userdetails(playername TEXT primary key, id TEXT, killspoint TEXT)")
    }

    override fun onUpgrade(DB: SQLiteDatabase, i: Int, ii: Int) {
        DB.execSQL("drop Table if exists Userdetails")
    }

    fun insertuserdata(playername: String?, id: String?, killspoint: String?): Boolean {
        val DB = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("name", playername)
        contentValues.put("contact", id)
        contentValues.put("dob", killspoint)
        val result = DB.insert("Userdetails", null, contentValues)
        return if (result == -1L) {
            false
        } else {
            true
        }
    }

    fun deletedata(name: String): Boolean {
        val DB = this.writableDatabase
        val cursor = DB.rawQuery("Select * from Userdetails where name = ?", arrayOf(name))
        return if (cursor.count > 0) {
            val result = DB.delete("Userdetails", "name=?", arrayOf(name)).toLong()
            if (result == -1L) {
                false
            } else {
                true
            }
        } else {
            false
        }
    }

    fun getdata(): Cursor {
        val DB = this.writableDatabase
        return DB.rawQuery("Select * from Userdetails", null)
    }
}


//GAME OVER EVENTS
import androidx.appcompat.app.AppCompatActivity
        import android.widget.TextView
        import android.widget.EditText
        import android.annotation.SuppressLint
        import android.os.Bundle
        import com.sandipbhattacharya.spaceshooter.R
        import android.content.Intent
        import android.view.View
        import com.sandipbhattacharya.spaceshooter.StartUp

class GameOver : AppCompatActivity() {
    var tvPoints: TextView? = null
    var num: TextView? = null
    var name: EditText? = null
    @SuppressLint("WrongViewCast", "MissingInflatedId")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_over)
        //this code access team player name id access and show
        name = findViewById(R.id.playername)
        num = findViewById(R.id.id)
        val username = intent.getStringExtra("keyplayername")
        val useridname = intent.getStringExtra("keyid")
        name?.setText(username)
        num?.setText(useridname)


//this part adds ups point after killing players
        val points = intent.extras!!.getInt("points")
        tvPoints = findViewById(R.id.tvPoints)
        tvPoints?.setText("" + points)
    }

    /// this function use to restar game again
    fun restart(view: View?) {
        val intent = Intent(this@GameOver, StartUp::class.java)
        startActivity(intent)
        finish()
    }

    fun exit(view: View?) {
        finish()
    }
}



//THIS IS THE START UP PART.

class StartUp : AppCompatActivity() {
    var teamname: EditText? = null
    var playeridgenerate: TextView? = null
    var tvPoints: TextView? = null
    var addingdatateamplayer: Button? = null
    var viewdata: Button? = null
    var delet: Button? = null
    var DB: DBHelper? = null
    @SuppressLint("MissingInflatedId")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.startup)

        fun teamplayer(view: View?) {

            teamname = findViewById(R.id.player)
            playeridgenerate = findViewById<View>(R.id.generatenumber) as TextView
            addingdatateamplayer = findViewById(R.id.dataview)
            //this part obtains  input data from team player and generate id number auto
            //this part allows user to input data and play game when he presses the button


            val myRandom = Random()
            val buttonGenerate = findViewById<View>(R.id.generate) as Button
            val textGenerateNumber = findViewById<View>(R.id.generatenumber) as TextView
            buttonGenerate.setOnClickListener { // TODO Auto-generated method stub
                textGenerateNumber.text = myRandom.nextInt(1000).toString()

//this code line store data sqllite and transfer data other activity player manu box show player name and id
                addingdatateamplayer?.setOnClickListener(View.OnClickListener {
                    val username = teamname?.getText().toString()
                    val useridname = playeridgenerate!!.text.toString()
                    val intent = Intent(this@StartUp, com.sandipbhattacharya.spaceshooter.GameOver::class.java)
                    intent.putExtra("keyplayername", username)
                    intent.putExtra("keyid", useridname)
                    startActivity(intent)
                })

            }
/// generate auto id number limit 1000 number id assign

        }

// this code path add all the data and show only throgh button
        teamname = findViewById(R.id.player)
        tvPoints = findViewById(R.id.kill)
        playeridgenerate = findViewById(R.id.generatenumber)
        add = findViewById(R.id.add)

        viewdata = findViewById(R.id.dataview)
        DB = DBHelper(this)
        add?.setOnClickListener(View.OnClickListener {
            val teamtxt = teamname?.getText().toString()
            val idtxt = playeridgenerate?.getText().toString()
            val killtxt = tvPoints?.getText().toString()
            val checkinsertdata = DB!!.insertuserdata(teamtxt, idtxt, killtxt)
        })

        viewdata?.setOnClickListener(View.OnClickListener {
            val res = DB!!.getdata()
            if (res.count == 0) {
                Toast.makeText(this@StartUp, "No Entry Exists", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            val buffer = StringBuffer()
            while (res.moveToNext()) {
                buffer.append(
                    """
    Player Name :${res.getString(0)}
    
    """.trimIndent()
                )
                buffer.append(
                    """
    Player Id :${res.getString(1)}
    
    """.trimIndent()
                )
                buffer.append(
                    """
    Player Kills:${res.getString(2)}
    
    
    """.trimIndent()
                )
            }
            val builder = AlertDialog.Builder(this@StartUp)
            builder.setCancelable(true)
            builder.setTitle("User Entries")
            builder.setMessage(buffer.toString())
            builder.show()
        })
    }

    fun startGame(view: View?) {
        startActivity(Intent(this, MainActivity::class.kotlin))
        finish()
    }


}