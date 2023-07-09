package com.example.notesapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.notesapp.R;

public class About_Us extends AppCompatActivity {

    ImageView imgLinkedin,imgGithub,imgInstagram;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        //After clicking the back home button
        findViewById(R.id.img_back_btn).setOnClickListener(v-> onBackPressed());

        //For social media links
        imgLinkedin = findViewById(R.id.img_linkedin);
        imgGithub = findViewById(R.id.img_github);
        imgInstagram = findViewById(R.id.img_instagram);

        //After clicking the links
        imgLinkedin.setOnClickListener(v -> goToUrl("https://www.linkedin.com/in/mohammed-sahil-512b4b210"));
        imgGithub.setOnClickListener(v -> goToUrl("https://github.com/MSahil33"));
        imgInstagram.setOnClickListener(v -> goToUrl("https://instagram.com/m_sahil.13?igshid=MzNlNGNkZWQ4Mg=="));

    }

    private void goToUrl(String link) {
        Uri uri = Uri.parse(link);
        startActivity(new Intent(Intent.ACTION_VIEW,uri));
    }
}