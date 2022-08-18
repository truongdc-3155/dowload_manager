package com.example.dowloadmanager

import android.app.DownloadManager
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var  downloadManager : DownloadManager

    private var btDownload : Button? = null
    private var btCancel : Button? = null
    private var btStatus : Button? = null

    val uri = Uri.parse("https://cdn.tgdd.vn/hoi-dap/812154/Ung-dung-hinh-nen-dep-Android-(35).jpg")
    val imageName  = "test.jpg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initView()

        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        btDownload?.setOnClickListener {
            //startDownload(uri)
            downloadManager(uri , imageName)
        }
        btCancel?.setOnClickListener {
            cancelDownload(0)
        }
        btStatus?.setOnClickListener {
            val status = checkStatus(0)
            Toast.makeText(this@MainActivity, "Message ${status}", Toast.LENGTH_SHORT).show()
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
            }
        }
    }

    private fun downloadManager(uri: Uri , outputFilename : String) {
        val request = DownloadManager.Request(uri)
        request.setTitle(imageName)
        request.setDescription("Downloading" +imageName)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.allowScanningByMediaScanner()
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS , outputFilename)
        val manager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }


    private fun cancelDownload(downloadId : Long){
        downloadManager.remove(downloadId)
        btDownload?.isEnabled = true
        btCancel?.isEnabled = false
    }

    private fun checkStatus(downloadId : Long): String {
        val query = DownloadManager.Query()
        query.setFilterById(downloadId)

        val cursor = downloadManager.query(query)
        if (cursor?.moveToFirst() == true) {
            return downloadStatus(cursor)
        }
        return "NO_STATUS_INFO"
    }

    private fun downloadStatus(cursor: Cursor): String {
        // column for download  status
        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
        val status = cursor.getInt(columnIndex)
        // column for reason code if the download failed or paused
        val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON)
        val reason = cursor.getInt(columnReason)

        var statusText = ""
        var reasonText = ""

        when (status) {
            DownloadManager.STATUS_FAILED -> {
                statusText = "STATUS_FAILED"
                when (reason) {
                    DownloadManager.ERROR_CANNOT_RESUME -> reasonText = "ERROR_CANNOT_RESUME"
                    DownloadManager.ERROR_DEVICE_NOT_FOUND -> reasonText = "ERROR_DEVICE_NOT_FOUND"
                    DownloadManager.ERROR_FILE_ALREADY_EXISTS -> reasonText = "ERROR_FILE_ALREADY_EXISTS"
                    DownloadManager.ERROR_FILE_ERROR -> reasonText = "ERROR_FILE_ERROR"
                    DownloadManager.ERROR_HTTP_DATA_ERROR -> reasonText = "ERROR_HTTP_DATA_ERROR"
                    DownloadManager.ERROR_INSUFFICIENT_SPACE -> reasonText = "ERROR_INSUFFICIENT_SPACE"
                    DownloadManager.ERROR_TOO_MANY_REDIRECTS -> reasonText = "ERROR_TOO_MANY_REDIRECTS"
                    DownloadManager.ERROR_UNHANDLED_HTTP_CODE -> reasonText = "ERROR_UNHANDLED_HTTP_CODE"
                    DownloadManager.ERROR_UNKNOWN -> reasonText = "ERROR_UNKNOWN"
                }
            }
            DownloadManager.STATUS_PAUSED -> {
                statusText = "STATUS_PAUSED"
                when (reason) {
                    DownloadManager.PAUSED_QUEUED_FOR_WIFI -> reasonText = "PAUSED_QUEUED_FOR_WIFI"
                    DownloadManager.PAUSED_UNKNOWN -> reasonText = "PAUSED_UNKNOWN"
                    DownloadManager.PAUSED_WAITING_FOR_NETWORK -> reasonText = "PAUSED_WAITING_FOR_NETWORK"
                    DownloadManager.PAUSED_WAITING_TO_RETRY -> reasonText = "PAUSED_WAITING_TO_RETRY"
                }
            }
            DownloadManager.STATUS_PENDING -> statusText = "STATUS_PENDING"
            DownloadManager.STATUS_RUNNING -> statusText = "STATUS_RUNNING"
            DownloadManager.STATUS_SUCCESSFUL -> statusText = "STATUS_SUCCESSFUL"
        }

        return "Download Status: $statusText, $reasonText"
    }

    private fun initView() {
        btDownload = findViewById(R.id.btDownload)
        btCancel = findViewById(R.id.btCancel)
        btStatus = findViewById(R.id.btStatus)
    }
}
