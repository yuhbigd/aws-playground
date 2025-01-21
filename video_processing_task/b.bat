ffmpeg -i input_10mb.mp4 
-filter_complex "[0:v]split=4[v1][v2][v3][v4]; 
[v1]scale='1920:1080'[v1out]; 
[v2]scale='1280:720'[v2out]; 
[v3]scale='640:480'[v3out]; 
[v4]scale='480:360'[v4out]" 
-map "[v1out]" -map 0:a -c:v:0 libx265 -crf 23 -b:v:0 10M -preset ultrafast -keyint_min 24 -level 3.0 -g 48 
-map "[v2out]" -map 0:a -c:v:1 libx265 -crf 23 -b:v:1 4M -preset ultrafast -keyint_min 24 -level 3.0 -g 48 
-map "[v3out]" -map 0:a -c:v:2 libx265 -crf 23 -b:v:2 2M -preset ultrafast -keyint_min 24 -level 3.0 -g 48 
-map "[v4out]" -map 0:a -c:v:3 libx265 -crf 23 -b:v:3 0.8M -preset ultrafast -keyint_min 24 -level 3.0 -g 48 
-pix_fmt yuv420p -r 23.976 
-var_stream_map "v:0,a:0 v:1,a:1 v:2,a:2 v:3,a:3" 
-hls_segment_filename v/vs%v/file_%03d.ts 
-master_pl_name master.m3u8 
-hls_list_size 0 
-hls_time 5 
-hls_playlist_type vod 
-hls_segment_type fmp4 
-master_pl_publish_rate 5 
v/vs%v/out.m3u8 
-y
