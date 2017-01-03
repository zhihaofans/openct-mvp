package cc.metapro.openct.libsearch;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cc.metapro.openct.R;
import cc.metapro.openct.data.BookInfo;

final class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.BookInfoViewHolder> {

    private Context mContext;

    private List<BookInfo> mBookInfos;

    public BooksAdapter(Context context) {
        mContext = context;
        mBookInfos = new ArrayList<>(0);
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

        @BindView(R.id.book_title)
        TextView mTitle;

        @BindView(R.id.author)
        TextView mAuthor;

        @BindView(R.id.content)
        TextView mContent;

        @BindView(R.id.store_info)
        TextView mStoreInfo;

        @BindView(R.id.load_raw)
        TextView mLink;

        public BookInfoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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
                    BookDetailActivity.actionStart(context, mTitle.getText().toString(), link);
                }
            });
        }
    }
}
