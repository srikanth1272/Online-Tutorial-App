package com.multipurposeapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.multipurposeapp.Utils.toastme
import com.multipurposeapp.adapters.ContentRecyclerAdapter
import com.multipurposeapp.adapters.HeaderRecyclerAdapter
import com.multipurposeapp.adapters.OnContentClick
import com.multipurposeapp.adapters.OnHeaderClick
import com.multipurposeapp.adapters.PaymentModel
import com.multipurposeapp.data.ContentModel
import com.multipurposeapp.data.HeaderModel
import com.multipurposeapp.databinding.ActivityHomeBinding
import com.multipurposeapp.utils.Constants
import com.multipurposeapp.utils.Constants.selectedContent
import com.multipurposeapp.utils.FirebaseBackgroundService


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private var mFirebaseDatabase: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    val TAG = HomeActivity::class.java.toString()
    private val contents = "Contents"

    var fuser: FirebaseUser? = null
    var selectedHeader = ""
    private var serviceIntent:Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // assigning ID of the toolbar to a variable
        val toolbar: Toolbar = binding.toolbar

        serviceIntent = Intent(this, FirebaseBackgroundService::class.java)
        startService(serviceIntent)

        // using toolbar as ActionBar
        setSupportActionBar(toolbar)
        val linearLayoutManager = LinearLayoutManager(applicationContext,LinearLayoutManager.HORIZONTAL, false)
        binding.headerRecyclerView.layoutManager = linearLayoutManager

        val linearLayoutManager1 = LinearLayoutManager(applicationContext,LinearLayoutManager.VERTICAL, false)
        binding.contentRecyclerView.layoutManager =linearLayoutManager1

        /*val linearLayoutManager1 = GridLayoutManager(applicationContext,2)
        binding.contentRecyclerView.layoutManager =linearLayoutManager1*/
        fuser = FirebaseAuth.getInstance().currentUser
        auth = Firebase.auth
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        myRef = mFirebaseDatabase?.reference
        //gotoChatActivity()
        //toastme(Constants.isInstructur.toString())
        loadHeader()
        loadContent(contents)
        if (Constants.isAdmin) {
            binding.add.visibility = View.VISIBLE
            binding.add.setOnClickListener {
                addHeaderAndContent()
            }
        }else{
            binding.add.visibility = View.GONE
        }
        binding.exit.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Constants.isFromInsIcon = false
            Constants.isAdmin = false
            Constants.isInstructur = false
            Constants.isPayment = false
            status("offline")
            startActivity(Intent(this,LoginActivity::class.java))
            finish()
        }
        if (Constants.isInstructur){
            binding.insProfile.visibility = View.VISIBLE
            binding.insProfile.setOnClickListener {
                Constants.isFromInsIcon = true
                startActivity(Intent(this,InstructorActivity::class.java))
            }
        }
        if (Constants.isInstructur){
            binding.insChat.visibility = View.VISIBLE
            binding.insChat.setOnClickListener {
                startActivity(Intent(this,ChatListActivity::class.java))
            }
        }
        //paymentMethod()
    }

    override fun onDestroy() {
        super.onDestroy()
        status("offline")
    }

    private fun addHeaderAndContent() {
        val builder = AlertDialog.Builder(this@HomeActivity)
        val inflater = layoutInflater
        //builder.setTitle("With EditText")
        val dialogLayout = inflater.inflate(R.layout.add_dialog, null)
        val header  = dialogLayout.findViewById<EditText>(R.id.header)
        val addHeader = dialogLayout.findViewById<Button>(R.id.addHeader)
        addHeader.setOnClickListener {
            val sHeader = header.text.toString()
            if (sHeader.isEmpty()){

            }else{
                addHeaderToDB(sHeader)
            }
        }
        builder.setView(dialogLayout)
        //builder.setPositiveButton("OK") { dialogInterface, i -> Toast.makeText(applicationContext, "EditText is " + editText.text.toString(), Toast.LENGTH_SHORT).show() }
        builder.show()
    }

    private fun addHeaderToDB(sHeader: String) {
        myRef = FirebaseDatabase.getInstance().getReference("Headers")
        val slotKey = myRef?.push()?.key!!
        val headerModel = HeaderModel(sHeader,slotKey)

        myRef?.child(slotKey)?.setValue(headerModel)?.addOnSuccessListener {
            toastme("Header Added!")
            loadHeader()
        }?.addOnFailureListener {

        }
    }

    private fun loadHeader() {
        val reference = FirebaseDatabase.getInstance().reference.child("Headers")
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val headerList = mutableListOf<HeaderModel>()
                for (childSnapshot in dataSnapshot.children) {
                    //Log.e(TAG,"childSnapshot $childSnapshot")
                    try {
                        val headerModel = childSnapshot.getValue(HeaderModel::class.java)
                        headerModel?.let {
                            headerList.add(it)
                        }
                        //Log.e(TAG,"headerList $headerList")
                    } catch (e: DatabaseException) {
                        e.printStackTrace()
                    }
                }

                val headerRecyclerAdapter = HeaderRecyclerAdapter(
                    applicationContext,
                    headerList,
                    object : OnHeaderClick{
                        override fun onClick(headerName: String) {
                            //toastme(headerName)
                            selectedHeader=headerName
                            if (Constants.isAdmin) {
                                addContentsForHeaders(headerName)
                                loadContent(headerName)
                            }
                            loadContent(headerName)
                        }

                        override fun onLongClick(headerModel: HeaderModel) {
                          val mPostReference = headerModel?.key?.let {
                                FirebaseDatabase.getInstance().reference
                                    .child("Headers").child(it)
                            }
                            mPostReference?.removeValue()
                            headerModel?.headerName?.let { toastme("$it Deleted!") }
                            loadHeader()
                        }
                    }
                )
                binding.headerRecyclerView.adapter = headerRecyclerAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG,"error $error")
            }
        })
    }

    private fun addContentsForHeaders(headerName: String) {
        myRef = FirebaseDatabase.getInstance().getReference(headerName)
        val headerToContentRef = FirebaseDatabase.getInstance().getReference(contents)

        val builder = AlertDialog.Builder(this@HomeActivity)
        val inflater = layoutInflater
        //builder.setTitle("With EditText")
        val dialogLayout = inflater.inflate(R.layout.add_dialog, null)
        val content  = dialogLayout.findViewById<EditText>(R.id.content)
        val addContent = dialogLayout.findViewById<Button>(R.id.addContent)
        addContent.setOnClickListener {
            val sContent = content.text.toString()
            if (sContent.isEmpty()){

            }else{
                val slotKey = myRef?.push()?.key!!
                val contentModel = ContentModel(sContent,"","",slotKey,headerName)
                myRef?.child(slotKey)?.setValue(contentModel)?.addOnSuccessListener {
                    toastme("Content Added!")

                    loadContent(headerName)
                }?.addOnFailureListener {

                }

                headerToContentRef?.push()?.setValue(contentModel)?.addOnSuccessListener {

                }?.addOnFailureListener {

                }
            }
        }
        builder.setView(dialogLayout)
        //builder.setPositiveButton("OK") { dialogInterface, i -> Toast.makeText(applicationContext, "EditText is " + editText.text.toString(), Toast.LENGTH_SHORT).show() }
        builder.show()


    }

    private fun loadContent(from:String) {
        if (from == contents){
            val reference = FirebaseDatabase.getInstance().reference.child(contents)
            reference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val contentList = mutableListOf<ContentModel>()
                    for (childSnapshot in dataSnapshot.children) {
                        Log.e(TAG,"childSnapshot $childSnapshot")
                        try {
                            val headerModel = childSnapshot.getValue(ContentModel::class.java)
                            headerModel?.let {
                                contentList.add(it)
                            }
                            Log.e(TAG, "contentList $contentList")
                        } catch (e: DatabaseException) {
                            e.printStackTrace()
                            Log.e(TAG, "error $e")
                        }
                    }

                    val contentRecyclerAdapter = ContentRecyclerAdapter(
                        applicationContext,
                        contentList,
                        object : OnContentClick {
                            override fun onClick(contentName: String) {
                                selectedContent = contentName
                                toastme(contentName)
                                Constants.insTitle = contentName
                                if (Constants.isInstructur) {
                                    gotoInsActivity()
                                }else{
                                    gotoInstructorListActivity()
                                }
                            }

                            override fun onLongClick(contentModel: ContentModel) {
                                selectedContent = contentModel?.contentName.toString()
                                Constants.insTitle = contentModel?.contentName.toString()
                                if (Constants.isInstructur) {
                                    Constants.isFromInsIcon = true
                                    gotoInsActivity()
                                }else if (Constants.isAdmin){
                                    deleteContent(contentModel)
                                }
                            }
                        }
                    )
                    binding.contentRecyclerView.adapter = contentRecyclerAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "error $error")
                }
            })
        }else {
            val reference = FirebaseDatabase.getInstance().reference.child(from)
            reference?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val contentList = mutableListOf<ContentModel>()
                    for (childSnapshot in dataSnapshot.children) {
                        //Log.e(TAG,"childSnapshot $childSnapshot")
                        try {
                            val contentModel = childSnapshot.getValue(ContentModel::class.java)
                            contentModel?.let {
                                contentList.add(it)
                            }
                            Log.e(TAG, "headerList $contentList")
                        } catch (e: DatabaseException) {
                            e.printStackTrace()
                            Log.e(TAG, "contentList $e")
                        }
                    }

                    val contentRecyclerAdapter = ContentRecyclerAdapter(
                        applicationContext,
                        contentList,
                        object : OnContentClick {
                            override fun onClick(contentName: String) {
                                selectedContent = contentName
                                Constants.insTitle = contentName
                                toastme(contentName)
                                if (Constants.isInstructur) {
                                    gotoInsActivity()
                                }else{
                                    gotoInstructorListActivity()
                                }
                            }

                            override fun onLongClick(contentModel: ContentModel) {
                                selectedContent = contentModel?.contentName.toString()
                                Constants.insTitle = contentModel?.contentName.toString()
                                if (Constants.isInstructur) {
                                    Constants.isFromInsIcon = true
                                    gotoInsActivity()
                                }else if (Constants.isAdmin){
                                    deleteContent(contentModel)
                                }
                            }
                        }
                    )
                    binding.contentRecyclerView.adapter = contentRecyclerAdapter
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "error $error")
                }
            })
        }
    }

    private fun status(status: String) {
        val reff = FirebaseDatabase.getInstance().getReference("Users").child(fuser?.uid.toString())
        val hashMap = HashMap<String, Any>()
        hashMap["status"] = status
        reff?.updateChildren(hashMap)
    }

    private fun deleteContent(contentModel: ContentModel) {
        /*val mPostReference = contentModel?.key?.let {
            FirebaseDatabase.getInstance().reference.child(contentModel.contentName!!).child(it)
        };
        //mPostReference?.removeValue()
        mPostReference?.removeValue(object : DatabaseReference.CompletionListener{
            override fun onComplete(error: DatabaseError?, ref: DatabaseReference) {
                toastme("error: $error")
            }
        })*/
        /*contentModel?.contentName?.let { toastme("$it Deleted!") }
        loadContent(contents)*/
    }

    private fun gotoInsActivity() {
        startActivity(
            Intent(this@HomeActivity,InstructorActivity::class.java))
    }

    private fun gotoInstructorListActivity() {
        startActivity(
            Intent(this@HomeActivity,InstructorListActivity::class.java))
    }

    private fun gotoChatActivity() {
        startActivity(Intent(this@HomeActivity,ChatListActivity::class.java)
            .putExtra(Constants.Content,selectedContent)
            .putExtra(Constants.Header,selectedHeader))
        //finish()
    }
    private fun paymentMethod(){
        myRef = FirebaseDatabase.getInstance().getReference("payment")
        val slotKey = myRef?.push()?.key!!
        val paymentModel = PaymentModel(Firebase.auth.currentUser?.email.toString(),
            "")
        myRef?.child(slotKey)?.setValue(paymentModel)?.addOnSuccessListener {
            // toastme("Content Added!")
        }?.addOnFailureListener {

        }
    }
}

