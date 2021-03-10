package com.finetech.fineevapp.ExportCSV;

import androidx.appcompat.app.AppCompatActivity;


import com.finetech.fineevapp.R;

public class ExportCVS extends AppCompatActivity {

//    String DATE;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.export_cvs);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//        }
//
//        Button btnExport = findViewById(R.id.btn_export);
//        btnExport.setOnClickListener(n->{
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            new ExportDatabaseCSVTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        } else {
//            new ExportDatabaseCSVTask().execute();
//        }
//
//        });
//    }
//    private void ShareFile() {
//
////        File exportDir = new File(Environment.getExternalStorageDirectory(), "/codesss/");
//        File exportDir = new File(getApplication().getExternalFilesDir("cvs"),"/codesss/");
//        String fileName = "FineEv.csv";
//        File sharingGifFile = new File(exportDir, fileName);
//        Intent shareIntent = new Intent(Intent.ACTION_SEND);
//        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        shareIntent.setPackage("com.google.android.gm");
////        shareIntent.setPackage("com.kakao.talk");
//        shareIntent.setType("application/csv");
//
//
////        Uri uri = Uri.fromFile(sharingGifFile);
//        Uri uri = FileProvider.getUriForFile(this,"com.finetech.fineevapp.fileprovider",sharingGifFile);
//
//        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//        startActivity(Intent.createChooser(shareIntent, "Share CSV"));
//    }
//
//
//    public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean> {
//
//        private final ProgressDialog dialog = new ProgressDialog(ExportCVS.this);
//        ElecDataBase dbhelper;
//        @Override
//        protected void onPreExecute() {
//            this.dialog.setMessage("Exporting database...");
//            this.dialog.show();
//            dbhelper = new ElecDataBase(ExportCVS.this);
//        }
//
//        protected Boolean doInBackground(final String... args) {
////            File exportDir = new File(Environment.getExternalStorageDirectory(), "/codesss/");
//            File exportDir = new File(getApplication().getExternalFilesDir("cvs"),"/codesss/");
//            if (!exportDir.exists()) { exportDir.mkdirs(); }
//
//
//            long now = System.currentTimeMillis();
//            Date date = new Date(now);
//            SimpleDateFormat sdfNOW = new SimpleDateFormat("yyyy.MM.dd");
//            DATE = sdfNOW.format(date);
//
//            SharedPreferences pref = getApplication().getSharedPreferences("info",MODE_PRIVATE);
//            String carName = pref.getString("CarName","");
//
//            File file = new File(exportDir, DATE+"_"+carName+"_"+"충전내역.csv");
//
//
//            try {
//                file.createNewFile();
//                CSVWriter csvWrite = new CSVWriter(new FileWriter(file));
//                Cursor curCSV = dbhelper.raw();
//                String[] colum= new String[12];
//                colum[0] = "Date";
//                colum[1] = "Full Battery";
//                colum[2] = "Charging Battery";
//                colum[3] = "Mode";
//                colum[4] = "Start Time";
//                colum[5] = "Charging Time";
//                colum[6] = "Finish Time";
//                colum[7] = "Distance";
//                colum[8] = "Voltage";
//                colum[9] = "Electric Current";
//                colum[10] = "efficiency";
//                colum[11] = "Temp";
//
//
//                csvWrite.writeNext(colum);
////                csvWrite.writeNext(curCSV.getColumnNames());
//
//                while(curCSV.moveToNext()) {
//                    String arrStr[]=null;
//                    String[] mySecondStringArray = new String[curCSV.getColumnNames().length];
//                    for(int i=0;i<curCSV.getColumnNames().length;i++)
//                    {
//                        mySecondStringArray[i] =curCSV.getString(i);
//                    }
//                    csvWrite.writeNext(mySecondStringArray);
//                }
//                csvWrite.close();
//                curCSV.close();
//                return true;
//            } catch (IOException e) {
//                e.printStackTrace();
//                return false;
//            }
//        }
//
//        protected void onPostExecute(final Boolean success) {
//            if (this.dialog.isShowing()) { this.dialog.dismiss(); }
//            if (success) {
//                Toast.makeText(ExportCVS.this, "Export successful!", Toast.LENGTH_SHORT).show();
//                ShareFile();
//            } else {
//                Toast.makeText(ExportCVS.this, "Export failed", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//



}
