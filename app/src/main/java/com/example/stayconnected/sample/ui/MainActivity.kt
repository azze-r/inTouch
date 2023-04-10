package com.example.stayconnected.sample.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DiffUtil
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.stayconnected.R
import com.example.stayconnected.sample.modele.SavedDate
import com.example.stayconnected.sample.util.SpotDiffCallback
import com.example.stayconnected.sample.worker.UploadWork
import com.example.stayconnected.sample.modele.Contact
import com.yuyakaido.android.cardstackview.*
import io.realm.Realm
import io.realm.kotlin.where
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), CardStackListener {

    private val drawerLayout by lazy { findViewById<DrawerLayout>(R.id.drawer_layout) }
    private val cardStackView by lazy { findViewById<CardStackView>(R.id.card_stack_view) }
    var manager : CardStackLayoutManager? = null
    var adapter : CardStackAdapter? = null
    var allContacts:ArrayList<Contact> =  ArrayList()
    var subContacts:ArrayList<Contact> =  ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val work = PeriodicWorkRequestBuilder<UploadWork>(24, TimeUnit.HOURS)
            .setInitialDelay(24, TimeUnit.HOURS)
            .build()

        WorkManager
            .getInstance(this)
            .enqueue(work)

        manager = CardStackLayoutManager(this, this)

        getContact()

        populateContacts()

        endView.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }


    private fun populateContacts(){
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return

        Realm.init(this)
        val realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        val savedDate = realm.where<SavedDate>().findFirst()
        realm.commitTransaction()

        val currentCal: Calendar = Calendar.getInstance()

        if (savedDate == null) {


            val newSavedDate = SavedDate(currentCal.timeInMillis)

            realm.beginTransaction()
            realm.copyToRealm(newSavedDate)
            realm.commitTransaction()

            if (allContacts.size>=3) {
                val contact1 = allContacts[(0 until allContacts.count()).random()]
                allContacts.remove(contact1)
                val contact2 = allContacts[(0 until allContacts.count()).random()]
                allContacts.remove(contact2)
                val contact3 = allContacts[(0 until allContacts.count()).random()]
                allContacts.remove(contact3)

                with(sharedPref.edit()) {
                    putString("id1", contact1.id.toString())
                    putString("id2", contact2.id.toString())
                    putString("id3", contact3.id.toString())
                    commit()
                }

                subContacts.add(contact1)
                subContacts.add(contact2)
                subContacts.add(contact3)

                drawerLayout.visibility = View.VISIBLE
                endView.visibility = View.GONE

                adapter = CardStackAdapter(createSpots())
                setupCardStackView()

            }

        } else {

            val savedCal: Calendar = Calendar.getInstance()

            savedCal.time = Date(savedDate.value)

            if (checkIfSameDay(currentCal, savedCal)) {

                val id1 = sharedPref.getString("id1", "").toString()
                val id2 = sharedPref.getString("id2", "").toString()
                val id3 = sharedPref.getString("id3", "").toString()

                if (id1 != "") {
                    for (contact in allContacts) {
                        if (contact.id.toString() == id1) {
                            subContacts.add(contact)
                        }
                    }
                }

                if (id2 != "") {
                    for (contact in allContacts) {
                        if (contact.id.toString() == id2)
                            subContacts.add(contact)
                    }
                }

                if (id3 != "") {
                    for (contact in allContacts) {
                        if (contact.id.toString() == id3)
                            subContacts.add(contact)
                    }
                }

            } else {

                val contact1 = allContacts[(0 until allContacts.count()).random()]
                allContacts.remove(contact1)
                val contact2 = allContacts[(0 until allContacts.count()).random()]
                allContacts.remove(contact2)
                val contact3 = allContacts[(0 until allContacts.count()).random()]
                allContacts.remove(contact3)

                with(sharedPref.edit()) {
                    putString("id1", contact1.id.toString())
                    putString("id2", contact2.id.toString())
                    putString("id3", contact3.id.toString())
                    commit()
                }

                subContacts.add(contact1)
                subContacts.add(contact2)
                subContacts.add(contact3)

                val newSavedDate = SavedDate(currentCal.timeInMillis)
                realm.beginTransaction()
                realm.insertOrUpdate(newSavedDate)
                realm.commitTransaction()
            }

            adapter = CardStackAdapter(createSpots())
            setupCardStackView()

            if (subContacts.isEmpty()) {
                drawerLayout.visibility = View.GONE
                endView.visibility = View.VISIBLE
            } else {
                drawerLayout.visibility = View.VISIBLE
                endView.visibility = View.GONE
            }

        }
    }

    private fun checkIfSameDay(cal1:Calendar, cal2:Calendar):Boolean{
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    override fun onBackPressed() {
        if (endView.isVisible)
            onBackPressedDispatcher.onBackPressed()
    }

    override fun onCardDragging(direction: Direction, ratio: Float) {
        Log.d("CardStackView", "onCardDragging: d = ${direction.name}, r = $ratio")
    }

    override fun onCardSwiped(direction: Direction) {
        Log.d("CardStackView", "onCardSwiped: p = ${manager!!.topPosition}, d = $direction")
        if (direction == Direction.Right){
            val intent = Intent(Intent.ACTION_VIEW)
            val uri = Uri.withAppendedPath(
                ContactsContract.Contacts.CONTENT_URI,
                subContacts[manager!!.topPosition-1].id.toString()
            )
            intent.data = uri
            this.startActivity(intent)
        }
        if (manager!!.topPosition == adapter!!.itemCount - 5) {
            paginate()
        }

        updateDrawer()
    }

    private fun updateDrawer() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return

        val arrayIds = ArrayList<String>()

        var id1 = sharedPref.getString("id1", "").toString()
        var id2 = sharedPref.getString("id2", "").toString()
        var id3 = sharedPref.getString("id3", "").toString()

        arrayIds.add(id1)
        arrayIds.add(id2)
        arrayIds.add(id3)

        var i = 0


        while (i < arrayIds.count()){

            val sub = subContacts[manager!!.topPosition-1].id.toString()
            val id = arrayIds[i].toString()

            Log.i("wtf","sub id $sub $id")

            if (sub == id) {
                if (i == 0){
                    with (sharedPref.edit()) {
                        putString("id1", "")
                        commit()
                    }
                }

                if (i == 1){
                    with (sharedPref.edit()) {
                        putString("id2", "")
                        commit()
                    }
                }

                if (i == 2){
                    with (sharedPref.edit()) {
                        putString("id3", "")
                        commit()
                    }
                }

            }

            i += 1

        }

        id1 = sharedPref.getString("id1", "").toString()
        id2 = sharedPref.getString("id2", "").toString()
        id3 = sharedPref.getString("id3", "").toString()


        if (id1 == "" && id2 == "" && id3 == ""){
            drawerLayout.visibility = View.GONE
            endView.visibility = View.VISIBLE
        }
        else{
            drawerLayout.visibility = View.VISIBLE
            endView.visibility = View.GONE
        }
    }

    override fun onCardRewound() {
        Log.d("CardStackView", "onCardRewound: ${manager!!.topPosition}")
    }

    override fun onCardCanceled() {
        Log.d("CardStackView", "onCardCanceled: ${manager!!.topPosition}")
    }

    override fun onCardAppeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardAppeared: ($position) ${textView.text}")
    }

    override fun onCardDisappeared(view: View, position: Int) {
        val textView = view.findViewById<TextView>(R.id.item_name)
        Log.d("CardStackView", "onCardDisappeared: ($position) ${textView.text}")
    }

    private fun setupCardStackView() {
        initialize()
    }

    private fun initialize() {
        manager!!.setStackFrom(StackFrom.None)
        manager!!.setVisibleCount(3)
        manager!!.setTranslationInterval(8.0f)
        manager!!.setScaleInterval(0.95f)
        manager!!.setSwipeThreshold(0.3f)
        manager!!.setMaxDegree(20.0f)
        manager!!.setDirections(Direction.HORIZONTAL)
        manager!!.setCanScrollHorizontal(true)
        manager!!.setCanScrollVertical(true)
        manager!!.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        manager!!.setOverlayInterpolator(LinearInterpolator())
        cardStackView.layoutManager = manager
        cardStackView.adapter = adapter
        cardStackView.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    private fun paginate() {
        val old = adapter!!.getSpots()
        val new = old.plus(createSpots())
        val callback = SpotDiffCallback(old, new)
        val result = DiffUtil.calculateDiff(callback)
        adapter!!.setSpots(new)
        result.dispatchUpdatesTo(adapter!!)
    }

    private fun createSpots(): List<Contact> {
        return subContacts
    }

    private fun getContact() {

        val projection = arrayOf(
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.HAS_PHONE_NUMBER,
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.LOOKUP_KEY
        )

        val contentResolver = contentResolver


        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,  // List of columns to retrieve
            null,  // name filter of which rows to return (eg. SQL WHERE), null so get all
            null,  // Selection args, param binding
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
         // Sort order
        if (cursor != null) { // No guarantee the resolver will return data, must sanity check
            val contacts: MutableList<String> =
                ArrayList()
            while (cursor.moveToNext()) {


                if ((cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)))!= "0" && cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)) != null) {
                    Log.i("tryhard",cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)))

                    contacts.add(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)))
                    val name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                    val id =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))

                    allContacts.add(Contact(id.toLong(), name, " ", " "))

                }
            }
            cursor.close() // Got our data, close the cursor to save memory

        }
    }



}
