package cc.metapro.openct.data;

import com.google.common.base.Strings;

/**
 * Created by jeffrey on 11/23/16.
 */

public class BorrowInfo {
    private String mBarCode, mAuthor, mBookTitle, mContent, mBorrowDate, mDueDate;

    public BorrowInfo(String barCode, String bookTitle, String author, String content, String borrowDate, String dueDate) {
        mBarCode = barCode;
        mBookTitle = bookTitle;
        mAuthor = author;
        mContent = content;
        mBorrowDate = borrowDate;
        mDueDate = dueDate;
    }

    public boolean isExceeded() {

        return false;
    }

    public String toFullString() {
        StringBuilder sb = new StringBuilder();
        if (!Strings.isNullOrEmpty(mBarCode)) sb.append("条码编号: ").append(mBarCode);
        if (!Strings.isNullOrEmpty(mBookTitle))
            sb.append("\n\n").append("书籍信息: ").append(mBookTitle);
        if (!Strings.isNullOrEmpty(mBorrowDate))
            sb.append("\n\n").append("借阅日期: ").append(mBorrowDate);
        if (!Strings.isNullOrEmpty(mDueDate)) sb.append("\n\n").append("到期时间: ").append(mDueDate);
        return sb.toString();
    }

    public String getBarCode() {
        return mBarCode;
    }

    public String getBookTitle() {
        return mBookTitle;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }

    public String getBorrowDate() {
        return "借阅时间  " + mBorrowDate;
    }

    public String getDueDate() {
        return "应还时间  " + mDueDate;
    }

    @Override
    public String toString() {
        return mBookTitle + "\n\n到期时间: " + mDueDate;
    }
}
