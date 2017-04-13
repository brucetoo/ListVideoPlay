package com.brucetoo.listvideoplay.demo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bruce Too
 * On 10/20/16.
 * At 10:13
 */

public class ListDataGenerater {

    static List<VideoModel> datas = new ArrayList<>();

    static {
        VideoModel m1 = new VideoModel();
        m1.coverImage = "http://android-imgs.25pp.com/fs08/2017/02/09/0/84219701e196f9b98565bd9d1c5072bc.jpg";
//        m1.videoUrl = "http://video.pp.cn/fs08/2017/02/09/9/200_b34d50d8820d19961f73f57359e4ca45.mp4";
        m1.videoUrl = "http://video.pp.cn/fs08/2017/01/16/3/200_528893ee2d1573573679809fb7a75b70.mp4";

        VideoModel m2 = new VideoModel();
        m2.coverImage = "http://android-imgs.25pp.com/fs08/2017/03/27/1/4bf77f572889c1ae186ccc29e4439be4.jpg";
        m2.videoUrl = "http://video.pp.cn/fs08/2017/03/27/1/200_fdacc7f4592b7c3eac4295239f922405.mp4";

        VideoModel m3 = new VideoModel();
        m3.coverImage = "http://android-imgs.25pp.com/fs08/2017/03/05/11/f74556e9d6e776a6abb85771f3632887.jpg";
        m3.videoUrl = "http://video.pp.cn/fs08/2017/03/05/7/200_817dc48c75d8bdab6be56b41f1c6fac0.mp4";

        VideoModel m4 = new VideoModel();
        m4.coverImage = "http://android-imgs.25pp.com/fs08/2017/04/01/2/522bf786ea8c063d31c9e2b54892f086.jpg";
        m4.videoUrl = "http://video.pp.cn/fs08/2017/04/01/3/41a840c8-a840-4f81-90ea-78393d1d5b33.mp4";

        VideoModel m5 = new VideoModel();
        m5.coverImage = "http://android-imgs.25pp.com/fs08/2017/04/07/9/e6cdf397bb1bf1dcd963560ae17017a4.jpg";
        m5.videoUrl = "http://video.pp.cn/fs08/2017/04/07/8/b416ce1e-a1e7-484b-95d8-2b1c839a2cab.mp4";

        VideoModel m6 = new VideoModel();
        m6.coverImage = "http://android-imgs.25pp.com/fs08/2017/01/07/11/79f91004a25ddbbeecb562bd4256d727.jpg";
        m6.videoUrl = "http://video.pp.cn/fs08/2017/01/07/7/200_237d8131df69467aee0ab0f28988a0df.mp4";

        VideoModel m7 = new VideoModel();
        m7.coverImage = "http://android-imgs.25pp.com/fs08/2017/02/23/2/48da103a3a21d8a1dea01570bc35de8e.jpg";
        m7.videoUrl = "http://video.pp.cn/fs08/2017/02/23/10/aa74cfad-fca1-4aa4-9969-4a22d0d2b45b.mp4";

        VideoModel m8 = new VideoModel();
        m8.coverImage = "http://android-imgs.25pp.com/fs08/2017/01/08/3/8a6040d0a4fad07180f8e3762f63a2ee.jpg";
        m8.videoUrl = "http://video.pp.cn/fs08/2017/01/08/3/200_abbb1c85c5c1d9d13cebb33ac7931ea3.mp4";

        VideoModel m9 = new VideoModel();
        m9.coverImage = "http://android-imgs.25pp.com/fs08/2017/01/14/5/8238328e751cabe493ec23f0721ab767.jpg";
        m9.videoUrl = "http://video.pp.cn/fs08/2017/01/14/6/200_95e2c6d6c267df6453c22e23fda0a5a5.mp4";

        VideoModel m10 = new VideoModel();
        m10.coverImage = "http://android-imgs.25pp.com/fs08/2017/04/11/1/cee48982ad11e3d333bfa6efaf72f12c.jpg";
        m10.videoUrl = "http://video.pp.cn/fs08/2017/04/11/7/200_b81b52e4df88bb878248623045d47cca.mp4";

        VideoModel m11 = new VideoModel();
        m11.coverImage = "http://android-imgs.25pp.com/fs08/2017/04/11/5/6d10e5650766c2260e5263d83b1aa2b0.jpg";
        m11.videoUrl = "http://video.pp.cn/fs08/2017/04/11/11/90751092-3f1d-403d-81c0-9cb8f512c9c1.mp4";

        VideoModel m12 = new VideoModel();
        m12.coverImage = "http://android-imgs.25pp.com/fs08/2017/04/11/8/54bdc1f5156cfc63005fd0fecd533897.jpg";
        m12.videoUrl = "http://video.pp.cn/fs08/2017/04/11/6/200_0c869e0dd681b98e459fad414a528005.mp4";

        datas.add(m1);
        datas.add(m2);
        datas.add(m3);
        datas.add(m4);
        datas.add(m5);
        datas.add(m6);
        datas.add(m7);
        datas.add(m8);
        datas.add(m9);
        datas.add(m10);
        datas.add(m11);
        datas.add(m12);
    }

//    public static List<VideoModel> getListData() {
//        return datas;
//    }
}
