package com.multipurposeapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.multipurposeapp.Utils.toastme
import com.multipurposeapp.data.HeaderModel
import com.multipurposeapp.data.InstructorModel
import com.multipurposeapp.databinding.ActivityInstructorBinding
import com.multipurposeapp.utils.Constants
import com.uploadcare.android.library.api.UploadcareClient
import com.uploadcare.android.library.exceptions.UploadFailureException
import com.uploadcare.android.library.upload.FileUploader
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class InstructorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInstructorBinding

    val TAG = InstructorActivity::javaClass.toString()
    var insName = ""
    val uploadcare = UploadcareClient(Constants.publicKey, Constants.secretKey)
    var insImageUrl = ""
    private lateinit var auth: FirebaseAuth
    private var mFirebaseDatabase: FirebaseDatabase? = null
    private var myRef: DatabaseReference? = null
    private var myRef2: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstructorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //insName = Constants.insName

        auth = Firebase.auth
        mFirebaseDatabase = FirebaseDatabase.getInstance()
        myRef = mFirebaseDatabase?.reference
        myRef2 = mFirebaseDatabase?.reference

        binding.profileImage.setOnClickListener {
            // PICK INTENT picks item from data
            // and returned selected item
            val galleryIntent = Intent(Intent.ACTION_PICK)
            // here item is type of image
            galleryIntent.type = "image/*"
            // ActivityResultLauncher callback
            imagePickerActivityResult.launch(galleryIntent)
        }
        binding.createMeeting.setOnClickListener {
            startActivity(Intent(this,JoinActivity::class.java))
        }
        if (Constants.isFromInsIcon){
            //toastme(Constants.isFromInsIcon.toString())
            loadInstructor()
        }else {
            val email = Firebase.auth.currentUser?.email.toString()
            binding.email.setText(email)
            uploadDataToFB()
        }

    }

    private fun loadInstructor() {
        val email = Firebase.auth.currentUser?.email.toString()
        val applesQuery: Query? = myRef?.
        child(Constants.selectedContent)?.orderByChild("email")?.equalTo(email)

        applesQuery?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (appleSnapshot in dataSnapshot.children) {
                    //Timber.e(TAG,appleSnapshot)
                    Log.e(TAG, "appleSnapshot $appleSnapshot")
                    val insmodel = appleSnapshot.getValue(InstructorModel::class.java)
                   // Log.e(TAG, "insmodel ${insmodel?.instructorImage}")
                    runOnUiThread {
                        binding.addDetailsButton.visibility = View.GONE
                        binding.createMeeting.visibility = View.VISIBLE
                        insName = insmodel?.instructorName.toString()
                        binding.name.setText(insmodel?.instructorName)
                        binding.phone.setText(insmodel?.phone)
                        binding.email.setText(insmodel?.email)
                        binding.dob.setText(insmodel?.dob)
                        binding.education.setText(insmodel?.education)
                        binding.achievements.setText(insmodel?.achievements)
                        binding.demoLinks.setText(insmodel?.demoLinks)
                        binding.studyTime.setText(insmodel?.studyTime)
                        binding.courseFee.setText(insmodel?.courseFee)
                        Glide.with(this@InstructorActivity)
                            .load(Constants.BaseUrl+insmodel?.instructorImage+Constants.PostUrl)
                            .into(binding.profileImage)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "onCancelled", databaseError.toException())
            }
        })
    }

    private fun uploadDataToFB() {

        binding.addDetailsButton.setOnClickListener {
            var name = binding.name.text.toString()
            var phone = binding.phone.text.toString()
            var email = binding.email.text.toString()
            var dob = binding.dob.text.toString()
            var education = binding.education.text.toString()
            var achievements = binding.achievements.text.toString()
            var demoLinks = binding.demoLinks.text.toString()
            var studyTime = binding.studyTime.text.toString()
            var courseFee = binding.courseFee.text.toString()

            if (name.isNotEmpty()||phone.isNotEmpty()||email.isNotEmpty()||
                dob.isNotEmpty()|| education.isNotEmpty()||
                achievements.isNotEmpty()||demoLinks.isNotEmpty()||studyTime.isNotEmpty()||
                courseFee.isNotEmpty()){

                myRef = FirebaseDatabase.getInstance().getReference(Constants.selectedContent)
                val slotKey = myRef?.push()?.key!!
                var instructorModel = InstructorModel(name,
                    Constants.selectedContent,phone,email,dob,education,
                    achievements,demoLinks,studyTime,courseFee,insImageUrl,slotKey)
                myRef?.child(slotKey)?.setValue(instructorModel)?.addOnSuccessListener {
                    toastme("Instructor Added!")
                    clearData()
                }?.addOnFailureListener {

                }

            }else{
                toastme("Please enter all fields!")
            }
        }
    }

    private fun clearData() {
        binding.name.setText("")
        binding.phone.setText("")
        binding.email.setText("")
        binding.dob.setText("")
        binding.education.setText("")
        binding.achievements.setText("")
        binding.demoLinks.setText("")
        binding.studyTime.setText("")
        binding.courseFee.setText("")
        binding.profileImage.setBackgroundResource(R.drawable.gallery)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private var imagePickerActivityResult: ActivityResultLauncher<Intent> =
    // lambda expression to receive a result back, here we
        // receive single item(photo) on selection
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e(TAG, "result->$result")
            if (result != null) {
                // getting URI of selected Image
                val imageUri: Uri? = result.data?.data

                GlobalScope.launch(Dispatchers.IO) {
                    // upload(imageUri)
                    val uploader = imageUri?.let {
                        FileUploader(
                            uploadcare,
                            it,
                            applicationContext
                        ) // Use "MultipleFilesUploader" for multiple files.
                            .store(true)
                    }
                    // Other upload parameters.

                    try {
                        val file = uploader?.upload()

                        Log.d(TAG, "file uuid: ${file?.uuid}")
                        Log.d(TAG, "file: ${Constants.BaseUrl+file?.uuid+Constants.PostUrl}")
                        insImageUrl = file?.uuid.toString()
                        runOnUiThread {
                            Glide.with(this@InstructorActivity)
                                .load(Constants.BaseUrl+file?.uuid+Constants.PostUrl)
                                .into(binding.profileImage)
                        }
                        // Successfully uploaded file to Uploadcare.
                    } catch (e: UploadFailureException) {
                        // Handle errors.
                    }
                }

            }
        }

    override fun onBackPressed() {
        super.onBackPressed()
        Constants.isFromInsIcon = false
    }

}