<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<title>upload</title>
<script type="text/javascript" src="/js/jquery-1.11.3.min.js"></script>
<script type="text/javascript"
	src="${myServer}/frontPages/js/browser-md5-file.min.js"></script>
<script type="text/javascript"
	src="${myServer}/frontPages/js/spark-md5.min.js"></script>
</head>
<body>
	<input type="file" name="file" id="file">
	<button id="upload" onClick="upload()">upload</button>
	<div id=bar1 style="display:">
		<table border="0" width="100%">
			<tr>
				<td><b>传送:</b></td>
			</tr>
			<tr bgcolor="#999999">
				<td>
					<table border="0" width="0%" cellspacing="1" bgcolor="#0033FF"
						id="PercentDone">
						<tr>
							<td>&nbsp;</td>
						</tr>
					</table>
				</td>
			</tr>
			<tr>
				<td>
					<table border="0" cellpadding="0" cellspacing="0">
						<tr>
							<td>上传状态:&nbsp</td>
							<td id="ProState" />
						</tr>
						<tr>
							<td>总 大 小:&nbsp</td>
							<td id="TotalSize" />
							<td>(字节)</td>
						</tr>
						<tr>
							<td>已经上传:&nbsp</td>
							<td id="SizeCompleted" />
							<td>(字节)</td>
						</tr>
						<tr>
							<td>平均速率:</td>
							<td id="TransferRate" />
							<td>(KB/秒)</td>
						</tr>
						<tr>
							<td>使用时间:</td>
							<td id="ElapsedTime" />
							<td>(毫秒)</td>
						</tr>
						<tr>
							<td>剩余时间:</td>
							<td id="TimeLeft" />
							<td>(毫秒)</td>
						</tr>
						<!-- <tr><td>错误信息:</td><td id="ErrMsg"></td></tr> -->
					</table>
				</td>
			</tr>
		</table>
	</div>
	<script type="text/javascript">
		var bytesPerPiece = 5 * 1024 * 1024; // 每个文件切片大小定为1MB .
		var totalPieces;
		var progressInfo = {
				speed : 0,
				totalSize : 0,
				uploadSize : 0,
				usedTime : 0,
				leftTime : 0,
				percent : 0
		}
		//发送请求
		function upload() {
			var blob = document.getElementById("file").files[0];
			var fileMD5;
			var start = 0;
			var end;
			var index = 0;
			var filesize = blob.size;
			var filename = blob.name;
			var timerId;
			$("#TotalSize").text(filesize);
			progressInfo.speed = 0;
			progressInfo.usedTime = 0;
			progressInfo.uploadSize = 0;
			progressInfo.percent = 0;
			progressInfo.totalSize = 0;
			progressInfo.totalSize = filesize;
			$("#ProState").html('<a style="color: #ff9900">准备上传</a>');
			setProgressInfo();
			//计算文件切片总数
			totalPieces = Math.ceil(filesize / bytesPerPiece);
			calculate(blob, function(md5) {
				timerId = setInterval(setProgressInfo,50);
				console.log(md5);
				var prepareDate = {
						originalFileName:filename,
						size:filesize,
						md5:md5,
						totalPart:totalPieces
				}
				$.ajax({
					url:'http://172.26.106.65:8089/file/prepare',
					data:JSON.stringify(prepareDate),
					type:"POST",
					contentType:"application/json",
					success:function(data){
					console.log(data)
					if(data.bingo){
						$("#ProState").html('<a style="color: #336600">上传完成</a>');
						alert("秒传")
						return;
					}
					$("#ProState").html('<a style="color: #3333cc">上传中</a>');
				while (start < filesize) {
					end = start + bytesPerPiece;
					if (end > filesize) {
						end = filesize;
					}

					var chunk = blob.slice(start, end);//切割文件    
					var sliceIndex = blob.name + index;
					var formData = new FormData();
					formData.append("file", chunk, filename);
					formData.append("index", index);
					formData.append("uploadId", data.uploadId);
					formData.append("totalParts", totalPieces);
					$.ajax({
						url : 'http://172.26.106.65:8089/file',
						//url :'http://172.26.106.65:8089/test/upload/ceph',
						type : 'POST',
						cache : false,
						data : formData,
						processData : false,
						contentType : false,
					}).done(function(res) {
						console.log(res);
						progressInfo.speed = res.speed;
						progressInfo.usedTime = res.usedTime;
						progressInfo.uploadSize = res.uploadSize;
						progressInfo.leftTime = res.leftTime;
						progressInfo.percent = (res.uploadSize/filesize)*100;
					}).fail(function(res) {

					});
					start = end;
					index++;
				}
				}
				})
			}); 
			
			
			function setProgressInfo(){
				$("#SizeCompleted").text(progressInfo.uploadSize)
				$("#TransferRate").text(progressInfo.speed)
				$("#ElapsedTime").text(progressInfo.usedTime)
				$("#PercentDone").attr("width",progressInfo.percent+"%")
				$("#TimeLeft").text(progressInfo.leftTime)
				if(progressInfo.uploadSize == progressInfo.totalSize){
					clearInterval(timerId);
					$("#ProState").html('<a style="color: #336600">上传完成</a>');
				}
			}
		}

		function calculate(file,callBack){  
		    var fileReader = new FileReader(),  
		        blobSlice = File.prototype.mozSlice || File.prototype.webkitSlice || File.prototype.slice,  
		        chunkSize = 5242880,  
		        // read in chunks of 2MB  
		        chunks = Math.ceil(file.size / chunkSize),  
		        currentChunk = 0,  
		        spark = new SparkMD5();  
		  
		    fileReader.onload = function(e) {  
		        spark.appendBinary(e.target.result); // append binary string  
		        currentChunk++;  
		  
		        if (currentChunk < chunks) {  
		            loadNext();  
		        }  
		        else {  
		            callBack(spark.end());
		        }  
		    };  
		  
		    function loadNext() {  
		        var start = currentChunk * chunkSize,  
		            end = start + chunkSize >= file.size ? file.size : start + chunkSize;  
		  
		        fileReader.readAsBinaryString(blobSlice.call(file, start, end));  
		    };  
		  
		    loadNext();  
		} 
	</script>
</body>
</html>