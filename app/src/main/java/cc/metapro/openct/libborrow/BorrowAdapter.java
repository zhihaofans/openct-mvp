package cc.metapro.openct.libborrow;

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
import cc.metapro.openct.data.BorrowInfo;

public class BorrowAdapter extends RecyclerView.Adapter<BorrowAdapter.BorrowViewHolder> {

    private List<BorrowInfo> mBorrows;

    private Context mContext;

    public BorrowAdapter(Context context) {
        mContext = context;
        mBorrows = new ArrayList<>(0);
    }

    @Override
    public BorrowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_borrow, parent, false);
        return new BorrowViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BorrowViewHolder holder, int position) {
        BorrowInfo b = mBorrows.get(position);
        holder.setTitle(b.getBookTitle());
        holder.setAuthor(b.getAuthor());
        holder.setContent(b.getContent());
        holder.setBorrowTime(b.getBorrowDate());
        holder.setDueTime(b.getDueDate());
    }

    @Override
    public int getItemCount() {
        return mBorrows.size();
    }

    public void setNewBorrows(List<BorrowInfo> borrows) {
        if (borrows == null || borrows.size() == 0) {
            mBorrows = new ArrayList<>(0);
        } else {
            mBorrows = borrows;
        }
    }

    public static class BorrowViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.lib_borrow_item_title)
        TextView mTitle;

        @BindView(R.id.lib_borrow_item_author)
        TextView mAuthor;

        @BindView(R.id.lib_borrow_item_borrow_content)
        TextView mContent;

        @BindView(R.id.lib_borrow_item_borrow_time)
        TextView mBorrowTime;

        @BindView(R.id.lib_borrow_item_due_time)
        TextView mDueTime;

        public BorrowViewHolder(View itemView) {
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

        public void setBorrowTime(String borrowTime) {
            mBorrowTime.setText(borrowTime);
        }

        public void setDueTime(String dueTime) {
            mDueTime.setText(dueTime);
        }
    }
}
