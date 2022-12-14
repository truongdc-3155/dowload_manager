# dowload_manager
Learn Dowload Manager

[Mục lục]

    -- 1. Khái niệm 
    -- 2. Permissions 
    -- 3. Generate URI 
    -- 4. Tạo instance cho DownloadManager
    -- 5. Dowload data
    -- 6. Check Download Status
    -- 7. Cancel downloads
    -- 8. Broadcast receiver    
 
# 1.[Khái niệm]
_ + Download manager là một service hệ thống dùng để xử lý các long-running HTTP downloads. Client 
có thể request một URI được download cho một file đích cụ thể. Download manager sẽ thực hiện 
download dưới background, nó quan tâm đến các HTTP interactions, retrying downloads sau khi có một 
lỗi hoặc thay đổi kết nối và các reboots hệ thống.

_ + Khi request download thông qua API này, ta nên register một broadcast receiver cho 
ACTION_NOTIFICATION_CLICKED để xử lý thích hợp khi người dùng click vào một running download hoặc 
từ download UI.

_ + Thể hiện của class phải được lấy từ Context.getSystemService(Class) với argument là 
DownloadManager.class hoặc Context.getSystemService(String)với argument là Context.DOWNLOAD_SERVICE.

# 2.[Permissions]
_ <uses-permission android:name="android.permission.INTERNET" />
_ <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> 
_ <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> 

# 3.[Generate URI]

  val imageUri = Uri.parse("http://commonsware.com/misc/test.mp4")
 
# 4.[Tạo instance cho DownloadManager]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Instances of download manager
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

# 5.[ Dowload data]

private fun startDownload(uri: Uri): Long {

        val downloadReference: Long

        val request = DownloadManager.Request(uri)

        // Setting title of request
        request.setTitle("Data Download")

        // Setting description of request
        request.setDescription("Android Data download using DownloadManager.")

        // Set the local destination for the downloaded file to a path
        // within the application's external files directory
        request.setDestinationInExternalFilesDir(this@MainActivity,Environment.DIRECTORY_DOWNLOADS, "test.mp4")
        // Enqueue download and save into referenceId
        downloadReference = downloadManager?.enqueue(request) ?: -1

        download.isEnabled = false
        downloadCancel.isEnabled = true

        return downloadReference
    }

[downloadReference]: Nó là id duy nhất để chỉ ra một download request.
[request]: Một request mới được tạo bằng việc sử dụng DownloadManager.Request(uri).
request.setDestinationInExternalFilesDir: Dùng để save file vào external downloads folder.
[downloadManager.enqueue(request)]: Enqueue một download mới tương ứng với request. Quá trình tải 
xuống sẽ tự động bắt đầu khi download manager sẵn sàng thực hiện và kết nối khả dụng.

# 6.[Check Download Status]

private fun getStatusMessage(downloadId: Long): String {

        val query = DownloadManager.Query()
        // set the query filter to our previously Enqueued download
        query.setFilterById(downloadId)

        // Query the download manager about downloads that have been requested.
        val cursor = downloadManager?.query(query)
        if (cursor?.moveToFirst() == true) {
            return downloadStatus(cursor)
        }
        return "NO_STATUS_INFO"
    }
    
DownloadManager.Query(): Được dùng để filter các download manager queries. Ở đây, ta cung cấp một 
downloadId trong setFilterById() để chỉ include các downloads với Id được cho.
downloadManager.query(query): Dùng để truy vấn download manager về downloads được request.
Cursor: Cursor trỏ đến các thông tin của downloads, với các cột bao gồm tất cả hằng số COLUMN_*.

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

# 7.[Cancel downloads]
downloadManager.remove(downloadId);

# 8.[ Broadcast receiver]

private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
override fun onReceive(context: Context, intent: Intent) {
// Download complete
// Check if the broadcast message is for our enqueued download
long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
}
}

    private var onNotificationClick: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // The download notification was clicked
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ...
        registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        registerReceiver(onNotificationClick, IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED))
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(onComplete)
        unregisterReceiver(onNotificationClick)
    }


[ACTION_DOWNLOAD_COMPLETE]: Broadcast intent action được gửi bởi download manager khi một download 
complete.
[ACTION_NOTIFICATION_CLICKED]: Broadcast intent action được gửi bởi download manager khi user clicks 
vào running download, từ system notification hoặc từ downloads UI. referenceId: Dùng để xác định 
download nào đã hoàn thành.





