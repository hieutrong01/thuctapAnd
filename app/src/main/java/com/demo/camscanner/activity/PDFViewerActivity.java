package com.demo.camscanner.activity;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.google.android.gms.ads.AdView;
import com.shockwave.pdfium.PdfDocument;
import com.demo.camscanner.R;
import com.demo.camscanner.utils.AdsUtils;

import java.util.List;

public class PDFViewerActivity extends BaseActivity implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {
    private static final String TAG = "PDFViewerActivity";
    private int page_no = 0;
    private PDFView pdfView;
    protected Uri pdf_uri;
    protected String title;
    protected TextView tv_page;
    protected TextView tv_title;
    private AdView adView;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_pdfviewer);
        init();
    }

    private void init() {
        adView = findViewById(R.id.adView);
        AdsUtils.showGoogleBannerAd(this, adView);

        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_page = (TextView) findViewById(R.id.tv_page);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfView.setBackgroundColor(getResources().getColor(R.color.bg_color));
        title = getIntent().getStringExtra("title");// Lấy giá trị của "extra" có tên "title" trong Intent và gán nó cho biến title.
        tv_title.setText(title);// Hiển thị giá trị của biến title lên tv_title
        // Lấy giá trị của "extra" có tên "pdf_path" trong Intent và tạo một Uri từ giá trị đó
        // . Giá trị Uri được gán cho biến pdf_uri.
        pdf_uri = Uri.parse(getIntent().getStringExtra("pdf_path"));
        loadPDF(pdf_uri);
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }
// nó sẽ tải một tài liệu PDF từ đường dẫn uri và hiển thị nó trên một thành phần pdfView.
    private void loadPDF(Uri uri) {
        // sử dụng hàm fromUri(uri) để chỉ định Uri của tài liệu PDF cần tải.
        //Hàm onPageChange(this) được sử dụng để lắng nghe sự thay đổi trang của tài liệu PDF.
        //Hàm enableAnnotationRendering(true) cho phép việc  xuất chú thích trên tài liệu PDF.
        //Hàm onLoad(this) được sử dụng để lắng nghe sự kiện tải xong của tài liệu PDF.
        //Hàm scrollHandle(new DefaultScrollHandle(this)) để sử dụng khi cuộn trang.
        //Hàm spacing(12) đặt khoảng cách giữa các trang.
        //hàm onPageError(this) được sử dụng để lắng nghe sự kiện lỗi khi tải trang.
        // hàm load() được gọi để thực hiện việc tải và hiển thị tài liệu PDF.
        pdfView.fromUri(uri).defaultPage(page_no).onPageChange(this).enableAnnotationRendering(true).onLoad(this).scrollHandle(new DefaultScrollHandle(this)).spacing(12).onPageError(this).load();
    }
    //Phương thức này sẽ được gọi mỗi khi tài liệu PDF đã được tải thành công
    //Nó lấy thông tin về tài liệu như tiêu đề, tác giả, chủ đề, từ khóa,
// người tạo, người sản xuất, ngày tạo và ngày chỉnh sửa từ đối tượng "PdfDocument.Meta".
    // Log.e() để giám sát hoạt động của chương trình.

    @Override
    public void loadComplete(int i) {
        PdfDocument.Meta documentMeta = pdfView.getDocumentMeta();
        Log.e(TAG, "title = " + documentMeta.getTitle());
        Log.e(TAG, "author = " + documentMeta.getAuthor());
        Log.e(TAG, "subject = " + documentMeta.getSubject());
        Log.e(TAG, "keywords = " + documentMeta.getKeywords());
        Log.e(TAG, "creator = " + documentMeta.getCreator());
        Log.e(TAG, "producer = " + documentMeta.getProducer());
        Log.e(TAG, "creationDate = " + documentMeta.getCreationDate());
        Log.e(TAG, "modDate = " + documentMeta.getModDate());
        printBookmarksTree(pdfView.getTableOfContents(), "-");
    }
//printBookmarksTree dùng để in ra cây đánh dấu (bookmark) của tài liệu PDF
    public void printBookmarksTree(List<PdfDocument.Bookmark> list, String str) {
        //list: Danh sách các đánh dấu của tài liệu PDF.
        //
        //str: Một chuỗi để thể hiện cấp độ của đánh dấu trong cây.
        for (PdfDocument.Bookmark next : list) {
            // sử dụng vòng lặp để duyệt qua mỗi đánh dấu trong danh sách. Với mỗi đánh dấu, chúng ta sẽ ghi ra tên,
            // trang mà đánh dấu nằm trong tài liệu PDF,
            //  gọi đến hàm printBookmarksTree để in ra cây đánh dấu con của nó (nếu có).
            //str + "-" sẽ được sử dụng để thể hiện cấp độ của các đánh dấu con.
            Log.e(TAG, String.format("%s %s, p %d", new Object[]{str, next.getTitle(), Long.valueOf(next.getPageIdx())}));
            if (next.hasChildren()) {
                List<PdfDocument.Bookmark> children = next.getChildren();
                printBookmarksTree(children, str + "-");
            }
        }
    }

    @Override
    public void onPageChanged(int i, int i2) {
        //Biến i là số trang hiện tại mà người dùng đang xem, và i2 là số trang trong tài liệu PDF.
        // giá trị của i được gán cho biến page_no, và giá trị của i + 1 được hiển thị trên một TextView là tv_page.
        page_no = i;
        tv_page.setText(String.format("%s / %s", new Object[]{Integer.valueOf(i + 1), Integer.valueOf(i2)}));
        //Ví dụ, nếu người dùng đang xem trang thứ 5 trong một tài liệu PDF có 10 trang,
        // giá trị của i sẽ là 4 và giá trị của i2 sẽ là 10. Khi đó, giá trị hiển thị trên TextView sẽ là "5 / 10".
    }

    @Override
    public void onPageError(int i, Throwable th) {
        Log.e(TAG, "Cannot load page " + i);
    }
}
