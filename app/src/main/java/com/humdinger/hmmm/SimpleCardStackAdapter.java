package com.humdinger.hmmm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
            //set the contents of the view
            //new LoadProfileImage(imageView).execute(model.getPhotoUrl());

            Picasso.with(getContext()).load(model.getPhotoUrl()).fit().into(imageView);
            usernameView.setText(model.getUsername());
            positionCompanyIndustryView.setText(model.getInfo());
            descriptionView.setText(model.getDescription());
        }

		return convertView;
	}


}
