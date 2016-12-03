package cc.metapro.openct.libsearch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cc.metapro.openct.R;
import cc.metapro.openct.data.BookInfo;

/**
 * Created by jeffrey on 11/29/16.
 */

public final class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookInfoViewHolder> {

    private Context mContext;

    private List<BookInfo> mBookInfos;

    public BooksAdapter(Context context, List<BookInfo> infos) {
        mContext = context;
        mBookInfos = infos;
        if (mBookInfos == null) {
            mBookInfos = new ArrayList<>(0);
        }
    }

    @Override
    public BookInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.item_book, parent, false);
        return new BookInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookInfoViewHolder holder, int position) {
        BookInfo b = mBookInfos.get(position);
        holder.setTitle(b.mTitle);
        holder.setAuthor(b.mAuthor);
        holder.setContent(b.mContent);
        holder.setStoreInfo(b.mStoreInfo);
        holder.setLoadRaw(mContext, b.mLink);
    }

    public void addBooks(List<BookInfo> infos) {
        for (BookInfo b : infos) {
            mBookInfos.add(b);
        }
    }

    public void addNewBooks(List<BookInfo> infos) {
        if (infos != null) {
            mBookInfos = infos;
        } else {
            mBookInfos = new ArrayList<>(0);
        }
    }

    @Override
    public int getItemCount() {
        return mBookInfos.size();
    }

    public static class BookInfoViewHolder extends RecyclerView.ViewHolder {

        private AppCompatTextView mTitle, mAuthor, mContent, mStoreInfo, mLink;

        public BookInfoViewHolder(View itemView) {
            super(itemView);
            mTitle = (AppCompatTextView) itemView.findViewById(R.id.book_title);
            mAuthor = (AppCompatTextView) itemView.findViewById(R.id.author);
            mContent = (AppCompatTextView) itemView.findViewById(R.id.content);
            mStoreInfo = (AppCompatTextView) itemView.findViewById(R.id.store_info);
            mLink = (AppCompatTextView) itemView.findViewById(R.id.load_raw);
        }

        public void setTitle(String title) {
            mTitle.setText(title);
        }

        public void setAuthor(String author) {
            mAuthor.setText(author);
        }

        public void setContent(String content) {
            mContent.setText(content);
        }

        public void setStoreInfo(String storeInfo) {
            mStoreInfo.setText(storeInfo);
        }

        public void setLoadRaw(final Context context, final String link) {
            mLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Intent intent = new Intent();
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse(link);
                        intent.setData(content_url);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
