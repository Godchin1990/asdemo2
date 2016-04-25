package rohan.rydo.com.rydofb;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;


public class Login extends ActionBarActivity {

    CallbackManager callbackManager;
    LoginButton login;
    String TAG = "hellofacebook";
    public static String id, name, hometown, location, birthday, friend_count;
    public static int age;
    public static ArrayList<String> institution = new ArrayList<>(), type = new ArrayList<>(), friends_list = new ArrayList<>(), position = new ArrayList<>(), workplace = new ArrayList<>();
    boolean friend_gotData = false, info_gotData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // initializing the facebook sdk
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_login);

        callbackManager = CallbackManager.Factory.create();
        login = (LoginButton) findViewById(R.id.login_button);
        login.setText("login");

        // set the required permissions
        login.setReadPermissions("public_profile email user_friends user_birthday user_education_history " +
                "user_hometown user_work_history user_location");

        login.setReadPermissions(Arrays.asList("basic_info", "email", "user_location", "user_birthday", "user_likes", "user_interests"));


        if (AccessToken.getCurrentAccessToken() != null) {
            RequestData();
        }

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AccessToken.getCurrentAccessToken() != null) {

                }
            }
        });

        ProfileTracker profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {

                Profile profile = Profile.getCurrentProfile();
                Log.i(TAG, "getFirstName=" + profile.getFirstName());
                Log.i(TAG, "getId=" + profile.getId());
                Log.i(TAG, "getLastName=" + profile.getLastName());
                Log.i(TAG, "getMiddleName=" + profile.getMiddleName());
                Log.i(TAG, "getName=" + profile.getName());
                Log.i(TAG, "getLinkUri=" + profile.getLinkUri());
                Log.i(TAG, "getProfilePictureUri=" + profile.getProfilePictureUri(200, 200));
            }
        };

        login.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {

                if (AccessToken.getCurrentAccessToken() != null) {
                    RequestData();
                }
                // do something
                queryPhotos(AccessToken.getCurrentAccessToken().getUserId());
                queryFriends1(AccessToken.getCurrentAccessToken().getUserId());
                queryFriends2(AccessToken.getCurrentAccessToken().getUserId());

                fetchUserInfo(AccessToken.getCurrentAccessToken());

                Log.i(TAG, "token=" + AccessToken.getCurrentAccessToken().getToken());
                Log.i(TAG, "userid=" + AccessToken.getCurrentAccessToken().getUserId());


                // token=CAAXTfNmOpsABAPOkDvqGTTZAZBgLaWnkCDwBuHLnLhl8x9AFxWufgjSX1VG1OKusRCs8RfkJVZBYt29Lmr1FBTzN0aORi2ZA4l3bVA7nKi9Uj2RaqONaZCyfSpiFiyw5J9priEMEvZCCffnJ6DhAmdL74ELKt15Yhv3jyau3gWMsBJOMVd5CqEICwfQ4OKaqi6lBXJzaZAOUQZDZD
                // userid=231865913846966
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }


        });


    }

    private void RequestData() {


        // to get the count of the friends and the list of friends using the app
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/friends",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        try {
                            Log.d(TAG, "friends count plus list " + response.getJSONObject());
                            JSONObject resp = new JSONObject("" + response.getJSONObject());
                            JSONArray data = resp.getJSONArray("data");
                            JSONObject summary = resp.getJSONObject("summary");
                            friend_count = summary.getString("total_count");
                            Log.d(TAG, "" + friend_count);

                            JSONObject friends;
                            for (int i = 0; i < data.length(); i++) {
                                friends = data.getJSONObject(i);
                                friends_list.add(friends.getString("name"));
                            }
                            Log.d(TAG, "friends test " + friends_list.toString());
                            friend_gotData = true;
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (friend_gotData && info_gotData) {
                            goToNext();
                        }
                    }
                }
        ).executeAsync();


        // to get basic information of the user
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject jsonObject, GraphResponse graphResponse) {

                Log.d(TAG, "" + jsonObject);
                String data = jsonObject.toString();

                try {
                    id = jsonObject.getString("id");
                    birthday = jsonObject.getString("birthday");
                    name = jsonObject.getString("name");
                    Log.i(TAG, "name=" + name);
                    birthday.replace("\\", "");
                    age = getAge(birthday);

                    JSONArray work = new JSONArray(jsonObject.getString("work"));
                    for (int i = 0; i < work.length(); i++) {
                        JSONObject working = work.getJSONObject(i);
                        position.add(new JSONObject(working.getString("position")).getString("name"));
                        workplace.add(new JSONObject(working.getString("employer")).getString("name"));
                    }
                    hometown = new JSONObject(jsonObject.getString("hometown")).getString("name");
                    JSONArray education = new JSONArray(jsonObject.getString("education"));

                    for (int i = 0; i < education.length(); i++) {
                        JSONObject edu = education.getJSONObject(i);
                        type.add(edu.getString("type"));
                        JSONObject school = new JSONObject(edu.getString("school"));
                        institution.add(school.getString("name"));
                    }
                    location = new JSONObject(jsonObject.getString("location")).getString("name");

                    info_gotData = true;
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "id " + id + " name " + name + " birthday " + birthday + " hometown " + hometown + " location " + location + " type " + type + " institution " + institution);
                if (friend_gotData && info_gotData) {
                    goToNext();
                }
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,age_range,location,work,birthday,hometown,education");
        request.setParameters(parameters);
        request.executeAsync();


    }

    void goToNext() {
        Intent intent = new Intent(this, Details.class);
        startActivity(intent);
    }

    public static int getAge(String dateOfBirth) {

        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();

        int age = 0;

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateOfBirth);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        birthDate.setTime(convertedDate);
        if (birthDate.after(today)) {
            throw new IllegalArgumentException("Can't be born in the future");
        }

        age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);

        // If birth date is greater than todays date (after 2 days adjustment of
        // leap year) then decrement age one year
        if ((birthDate.get(Calendar.DAY_OF_YEAR)
                - today.get(Calendar.DAY_OF_YEAR) > 3)
                || (birthDate.get(Calendar.MONTH) > today.get(Calendar.MONTH))) {
            age--;

            // If birth date and todays date are of same month and birth day of
            // month is greater than todays day of month then decrement age
        } else if ((birthDate.get(Calendar.MONTH) == today.get(Calendar.MONTH))
                && (birthDate.get(Calendar.DAY_OF_MONTH) > today
                .get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        return age;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }


    private void queryPhotos(String id) {
        new GraphRequest(AccessToken.getCurrentAccessToken(), id + "/picture", null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                JSONObject jsonObject = response.getJSONObject();
                response.getJSONArray();

                String s = jsonObject.toString();
                Log.d(TAG, "response:" + s);

                Log.d(TAG, "response:" + response.getRawResponse());

                /**
                 *  D/FACEBOOKSDK: {"id":"231865913846966","age_range":{"min":13,"max":17},"name":"沈彤"}
                 full data json{"id":"231865913846966","age_range":{"min":13,"max":17},"name":"沈彤"}
                 id 231865913846966 name null birthday null hometown null location null type [] institution []
                 response:{"data":[]}
                 friends count plus list {"summary":{"total_count":23},"data":[]}
                 23
                 friends test []

                 */


            }
        }).executeAsync();


    }

    private void queryFriends1(String id) {
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/friends", null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                JSONObject jsonObject = response.getJSONObject();
                response.getJSONArray();

                String s = jsonObject.toString();
                Log.d(TAG, "friends:" + s);

                Log.d(TAG, "friends:" + response.getRawResponse());

                /**
                 *  D/FACEBOOKSDK: {"id":"231865913846966","age_range":{"min":13,"max":17},"name":"沈彤"}
                 full data json{"id":"231865913846966","age_range":{"min":13,"max":17},"name":"沈彤"}
                 id 231865913846966 name null birthday null hometown null location null type [] institution []
                 response:{"data":[]}
                 friends count plus list {"summary":{"total_count":23},"data":[]}
                 23
                 friends test []

                 */


            }
        }).executeAsync();


    }

    private void queryFriends2(String id) {
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/friends", null, HttpMethod.GET, new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                JSONObject jsonObject = response.getJSONObject();
                response.getJSONArray();

                String s = jsonObject.toString();
                Log.d(TAG, "friends:" + s);

                Log.d(TAG, "friends:" + response.getRawResponse());

                /**
                 *  D/FACEBOOKSDK: {"id":"231865913846966","age_range":{"min":13,"max":17},"name":"沈彤"}
                 full data json{"id":"231865913846966","age_range":{"min":13,"max":17},"name":"沈彤"}
                 id 231865913846966 name null birthday null hometown null location null type [] institution []
                 response:{"data":[]}
                 friends count plus list {"summary":{"total_count":23},"data":[]}
                 23
                 friends test []

                 */


            }
        }).executeAsync();


    }


    private void fetchUserInfo(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object,
                                            GraphResponse response) {
                        try {
                            if (response.getError() != null) {
                                String errorMessage = response.getError().
                                        getErrorMessage();
                            } else {
                                User user = new User();
                                user.setEmail(object.getString("email"));
                                user.setGender(object.getString("gender"));
                                user.setLink(object.getString("link"));
                                user.setFirstname(object.getString("first_name"));
                                user.setLastname(object.getString("last_name"));
                                user.setLocale(object.getString("locale"));
                                user.setTimezone(object.getString("timezone"));
                                user.setUserId(object.getString("id"));
                                user.setUserName(object.getString("name"));

                                Log.d(TAG, "user:" + user.toString());

                            }
                        } catch (Exception e) {

                        }
                    }
                });
        request.executeAsync();
    }
}