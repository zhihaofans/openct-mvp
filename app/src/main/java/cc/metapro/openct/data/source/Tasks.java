package cc.metapro.openct.data.source;

import android.os.AsyncTask;

import cc.metapro.openct.university.CMS.UniversityCMS;
import cc.metapro.openct.university.Library.UniversityLibrary;

/**
 * Created by jeffrey on 11/30/16.
 */

public class Tasks extends AsyncTask<Integer, Integer, Integer> {

    private UniversityLibrary mLibrary;

    private UniversityCMS mCMS;

    @Override
    protected void onPostExecute(Integer integer) {
        super.onPostExecute(integer);
    }

    @Override
    protected Integer doInBackground(Integer... integers) {

        return null;
    }
}
