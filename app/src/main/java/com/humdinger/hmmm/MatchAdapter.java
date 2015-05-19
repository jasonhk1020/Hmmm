package com.humdinger.hmmm;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.Query;

import java.io.InputStream;

/**
 * @author greg
 * @since 6/21/13
 *
 * This class is an example of how to use FirebaseListAdapter. It uses the <code>Chat</code> class to encapsulate the
 * data for each individual chat message
 */
public class MatchAdapter extends FirebaseListAdapter<Match> {

    // The mUsername for this client. We use this to indicate which users to find not including user (eventually not working yet)
    private String mUsername;
    //private int index;

    public MatchAdapter(Query ref, Activity activity, int layout, String mUsername) {
        super(ref, Match.class, layout, activity);
        this.mUsername = mUsername;
        //this.index = index;


    }

    /**
     * Bind an instance of the <code>Chat</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single <code>Chat</code> instance that represents the current data to bind.
     *
     * @param view A view instance corresponding to the layout we passed to the constructor.
     * @param match An instance representing the current state of a chat message
     */
    @Override
    protected void populateView(View view, Match match) {
        // get uid
        String uid = match.getUid();

        //photo
        String photoUrl = match.getPhotoUrl();
        ImageView imageView = (ImageView) view.findViewById(R.id.match_image);
        new LoadProfileImage(imageView).execute(photoUrl);

        //name
        String username = match.getUsername();
        TextView usernameView = (TextView) view.findViewById(R.id.match_username);
        usernameView.setText(removeNull(username));

        //position company and industry into sentence
        String position = removeNull(match.getPosition());
        String company = removeNull(match.getCompany());
        String industry = removeNull(match.getIndustry());
        Spannable sPosition = new SpannableString(position + " at ");
        Spannable sCompany = new SpannableString(company + " in ");
        Spannable sIndustry = new SpannableString(industry);
        sPosition.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, position.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, company.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, industry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        //write info to view
        TextView positionCompanyIndustryView = (TextView) view.findViewById(R.id.match_position_company_industry);
        positionCompanyIndustryView.setText(TextUtils.concat(sPosition, sCompany, sIndustry));

        //description
        String description = match.getDescription();
        TextView descriptionView = (TextView) view.findViewById(R.id.match_description);
        descriptionView.setText(removeNull(description));

    }

    private String removeNull(String string) {
        if (string == null) {
            string = "";
        }
        return string;
    }

    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }
}
