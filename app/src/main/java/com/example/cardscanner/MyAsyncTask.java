package com.example.cardscanner;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;


public class MyAsyncTask extends AsyncTask<Void, Void, Void> {
    String mTAG = "myAsyncTask";
    private Context context;


    public MyAsyncTask(Context c) {
        super();
        context = c;
    }

    @Override
    protected void onPreExecute() {
        Toast T = Toast.makeText(
                context,
                R.string.toast_processing_img,
                Toast.LENGTH_LONG
        );
        T.show();
        Toast T1 = Toast.makeText(
                context,
                R.string.toast_processing_img_still,
                Toast.LENGTH_LONG
        );
        T1.show();
    }

    @Override
    protected Void doInBackground(Void... arg) {

        MainActivity.processBitmapImg(MainActivity.capturedImg, context);
        return null;

    }


    @Override
    protected void onPostExecute(Void a) {

        if (!MainActivity.scannedCardMat.empty() && !MainActivity.detectedFaceMat.empty()) {
            Intent reviewScannedCard = new Intent(context, ReviewScannedCard.class);
            reviewScannedCard.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            reviewScannedCard.putExtra("imgURI", MainActivity.currentScannedCardPhotoPath);
            context.startActivity(reviewScannedCard);
        } else {
            Toast T1 = Toast.makeText(context,
                    R.string.toast_no_face_found,
                    Toast.LENGTH_LONG);
            T1.show();

        }
    }
}