package com.humdinger.hmmm;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public final class SimpleCardStackAdapter extends CardStackAdapter {

	public SimpleCardStackAdapter(Context mContext) {
		super(mContext);
	}

	@Override
	public View getCardView(int position, CardModel model, View convertView, ViewGroup parent) {
		if(convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.std_card_inner, parent, false);
			assert convertView != null;
		}

        //setup the view
        final ImageView imageView = (ImageView) convertView.findViewById(R.id.match_image);
        final TextView usernameView = (TextView) convertView.findViewById(R.id.match_username);
        final TextView positionCompanyIndustryView = (TextView) convertView.findViewById(R.id.match_position_company_industry);
        final TextView descriptionView = (TextView) convertView.findViewById(R.id.match_description);

        if(model.getUid() != null) {
            //update views with info
            Firebase matchRef = new Firebase(convertView.getResources().getString(R.string.FIREBASE_URL)).child("users").child(model.getUid());
            matchRef.addValueEventListener(new ValueEventListener() {
                // Retrieve new posts as they are added to Firebase
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    //retrieve current user info from firebase
                    Map<String, String> map = (HashMap<String, String>) snapshot.getValue();
                    String username = removeNull(map.get("username"));
                    String position = removeNull(map.get("position"));
                    String company = removeNull(map.get("company"));
                    String industry = removeNull(map.get("industry"));
                    String description = removeNull(map.get("description"));
                    String photoUrl = removeNull(map.get("photoUrl"));

                    //position company and industry into sentence
                    Spannable sPosition = new SpannableString("");
                    Spannable sCompany = new SpannableString("");
                    Spannable sIndustry = new SpannableString("");

                    //logic for adding conjunctions and bolding
                    if (!position.equals("")) {
                        sPosition = new SpannableString(position);
                        sPosition.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sPosition.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    if (!company.equals("")) {
                        if (!position.equals("")) {
                            sCompany = new SpannableString(" at " + company);
                            sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            sCompany = new SpannableString(company);
                            sCompany.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sCompany.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                    if (!industry.equals("")) {
                        if (!position.equals("") || !company.equals("")) {
                            sIndustry = new SpannableString(" in " + industry);
                            sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 4, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else {
                            sIndustry = new SpannableString(industry);
                            sIndustry.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, sIndustry.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }

                    //set the contents of the view
                    new LoadProfileImage(imageView).execute(photoUrl);
                    usernameView.setText(removeNull(username));
                    positionCompanyIndustryView.setText(TextUtils.concat(sPosition, sCompany, sIndustry));
                    descriptionView.setText(removeNull(description));
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                }
            });
        }

		return convertView;
	}

    private String removeNull(String string) {
        if (string == null) {
            string = "";
        }
        return string;
    }
}
