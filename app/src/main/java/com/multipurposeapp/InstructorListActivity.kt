package com.multipurposeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.multipurposeapp.Utils.toastme
import com.multipurposeapp.adapters.IClick
import com.multipurposeapp.adapters.InstructorRecyclerAdapter
import com.multipurposeapp.adapters.PaymentModel
import com.multipurposeapp.data.InstructorModel
import com.multipurposeapp.databinding.ActivityInstructorListBinding
import com.multipurposeapp.utils.Constants
import java.util.concurrent.TimeUnit


class InstructorListActivity : AppCompatActivity() {
    private lateinit var binding:ActivityInstructorListBinding
    private lateinit var auth: FirebaseAuth
    private var mFirebaseDatabase: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    val TAG = InstructorListActivity::class.java.toString()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstructorListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val linearLayoutManager1 = LinearLayoutManager(applicationContext,
            LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewInstructorList.layoutManager =linearLayoutManager1

        binding.toolbar.title = Constants.insTitle

        auth = Firebase.auth
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        myRef = mFirebaseDatabase?.reference

        loadInstructorList()
    }

    private fun loadInstructorList() {
        val reference = FirebaseDatabase.getInstance().reference.child(Constants.selectedContent)
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val instructorList = mutableListOf<InstructorModel>()
                for (childSnapshot in dataSnapshot.children) {
                    Log.e(TAG,"childSnapshot $childSnapshot")
                    try {
                        val instructorModel = childSnapshot.getValue(InstructorModel::class.java)
                        instructorModel?.let {
                            instructorList.add(it)
                        }
                        Log.e(TAG,"instructorModel $instructorList")
                    } catch (e: DatabaseException) {
                        e.printStackTrace()
                    }
                }

                val instructorRecyclerAdapter = InstructorRecyclerAdapter(
                    applicationContext,
                    instructorList,
                    object : IClick {
                        override fun youtube(link: String) {
                            if (link.isNotEmpty()) {
                                val intent = Intent(
                                    Intent.ACTION_VIEW, Uri.parse(
                                        link
                                    )
                                )
                                startActivity(intent)
                            }
                        }

                        override fun chat(instructorModel: InstructorModel) {
                            startActivity(
                                Intent(this@InstructorListActivity, ChatListActivity::class.java)
                            )
                        }

                        override fun video(instructorModel: InstructorModel) {
                            if (Constants.isPayment){
                                paymentCompleted(instructorModel)
                            }else{
                                paymentMethod(instructorModel)
                            }
                        }

                        override fun onLongClick(instructorModel: InstructorModel) {
                            deleteItem(instructorModel)
                        }
                    }
                )
                binding.recyclerViewInstructorList.adapter = instructorRecyclerAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG,"error $error")
            }
        })
    }

    private fun deleteItem(instructorModel: InstructorModel) {

    }

    private fun paymentMethod(instructorModel: InstructorModel) {
        val builder = AlertDialog.Builder(this@InstructorListActivity).create()
        val inflater = layoutInflater
        //builder.setTitle("With EditText")
        val dialogLayout = inflater.inflate(R.layout.payment_dialog, null)
        val name  = dialogLayout.findViewById<EditText>(R.id.name)
        val upiId = dialogLayout.findViewById<EditText>(R.id.upiId)
        val price = dialogLayout.findViewById<EditText>(R.id.price)
        price.setText(instructorModel?.courseFee)

        val payment = dialogLayout.findViewById<Button>(R.id.payment)
        payment.setOnClickListener {
            val sname = name.text.toString()
            val supiIdn = upiId?.text.toString()
            val sprice = price?.text.toString()
            if (sname.isEmpty()||supiIdn.isEmpty()||sprice.isEmpty()){
                toastme("You cant add empty data!")
            }else{
                object: CountDownTimer(10000, 1000){
                    override fun onTick(p0: Long) {
                        runOnUiThread {
                            val milliSec = TimeUnit.MILLISECONDS.toSeconds(p0)
                            payment.text = "Payment processing $milliSec"
                        }

                    }
                    override fun onFinish() {
                        //add your code here
                        myRef = FirebaseDatabase.getInstance().getReference("payment")
                        val slotKey = myRef?.push()?.key!!
                        val paymentModel = PaymentModel(Firebase.auth.currentUser?.email.toString(),
                            instructorModel?.insMail.toString())
                        myRef?.child(slotKey)?.setValue(paymentModel)?.addOnSuccessListener {
                           // toastme("Content Added!")
                            builder.dismiss()
                            Constants.isPayment = true
                            paymentCompleted(instructorModel)
                        }?.addOnFailureListener {

                        }
                    }
                }.start()

            }
        }
        builder.setView(dialogLayout)
        //builder.setPositiveButton("OK") { dialogInterface, i -> Toast.makeText(applicationContext, "EditText is " + editText.text.toString(), Toast.LENGTH_SHORT).show() }
        builder.show()
    }

    private fun paymentCompleted(instructorModel: InstructorModel) {
        Constants.instructorModel?.instructorName = instructorModel.instructorName
        startActivity(
            Intent(this@InstructorListActivity, JoinActivity::class.java)
        )
    }

            private fun gotoInsActivity(insName: String) {
        /*Constants.insName = insName
        startActivity(
            Intent(this@InstructorListActivity,InstructorActivity::class.java)
            .putExtra(Constants.insName,insName))*/

    }
}