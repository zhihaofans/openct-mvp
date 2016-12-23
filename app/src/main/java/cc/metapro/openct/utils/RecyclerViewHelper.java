package cc.metapro.openct.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.animation.OvershootInterpolator;

import jp.wasabeef.recyclerview.adapters.AlphaInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.SlideInLeftAnimator;

/**
 * Created by jeffrey on 16/12/5.
 */

public class RecyclerViewHelper {

    public static LinearLayoutManager setRecyclerView(Context context, @NonNull RecyclerView recyclerView, RecyclerView.Adapter adapter) {
        LinearLayoutManager manager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);

        SlideInLeftAnimator animator = new SlideInLeftAnimator();
        animator.setInterpolator(new OvershootInterpolator());
        recyclerView.setItemAnimator(animator);

        recyclerView.setAdapter(new AlphaInAnimationAdapter(adapter));

        return manager;
    }
}
