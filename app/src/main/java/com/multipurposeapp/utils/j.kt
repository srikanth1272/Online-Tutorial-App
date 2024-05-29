package com.multipurposeapp.utils

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.multipurposeapp.adapters.PaymentModel

class j {
    /*val applesQuery: Query? =
        myRef?.child("payment")?.orderByChild("userMailId")
            ?.equalTo(Firebase.auth?.currentUser?.email)

    applesQuery?.addListenerForSingleValueEvent(object :
        ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Log.e(TAG,"dataSnapshotPayment: $dataSnapshot")
            for (appleSnapshot in dataSnapshot.children) {
                Log.e(TAG,"dataSnapshotPayment in: $dataSnapshot")
                val paymentModel =
                    appleSnapshot.getValue(PaymentModel::class.java)
                if (paymentModel?.insMail==""){
                    Log.e(TAG,"paymentModel?.insMail ${paymentModel?.insMail}")
                    paymentMethod(instructorModel)
                }else{
                    Log.e(TAG,"else")
                    if (paymentModel?.insMail==instructorModel?.insMail) {
                        paymentCompleted(instructorModel)
                    }else{
                        paymentMethod(instructorModel)
                    }
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            // paymentMethod(instructorModel)
            Log.e(TAG,"dataSnapshotPayment: error $error")
        }
    })*/
}