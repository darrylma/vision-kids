package com.my.digi.visionkids;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.text.format.Time;
import android.text.method.ScrollingMovementMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.Landmark;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class Capture extends Activity implements View.OnClickListener, TextToSpeech.OnInitListener {

	boolean myAudioToggle = true;
	boolean breedFound = false;
	boolean textOnClick, imageOnClick;
	Button bSearch, bSound, bUpload, bPlay, bClose, bBrowse;
	Canvas canvas;
	float scaleX, scaleY, scaleWidth, scaleHeight;
	ImageView ivPreview, ivPreviewProfile, ivPreviewProfileCircle, ivFriendlinessStars, ivPlayfulnessStars;
	int x, y, startX, startY, cropX, cropY, cropWidth, cropHeight, width, height, breedIndex;
	int defaultivHeight, targetivHeight, defaultivWidth, targetivWidth, defaultivLeft, defaultivTop;
	Paint paint;
	String objectResults;
	String[] objectsArray, objectsMidArray, printResultsArray;
	//String[] dogBreedArray, dogCategoryArray, dogLifespanArray, dogImageNameArray, dogMidArray;
	//int [] dogFriendlinessArray, dogPlayfulnessArray;
	float[] objectsScoreArray;
	SurfaceHolder holderTransparent;
	SurfaceView transparentView;
	TextView tvObjectDetails, tvProfile, tvNotification, tvInstructions, tvProfileName, tvProfileBackground;
	TextView tvObject1, tvObject2, tvObject3;
	private static final int GALLERY_IMAGE_REQUEST = 1;
	private static final int REQUEST_CROP_ICON = 2;
	//private static final String CLOUD_VISION_API_KEY = "AIzaSyCdm3gfCWDVmVm4Vhs43a4QN4upKjA7bfM";
	//private static final String CLOUD_VISION_API_KEY = "AIzaSyAajx889Bm3dN6Ga1VU6ZAzqLmQEz7sPy8";
	//private static final String CLOUD_VISION_API_KEY = "AIzaSyBC_HGuCjfOxQ5tKB31vEya3aUUjdx3wR0";
	private static final String CLOUD_VISION_API_KEY = "AIzaSyBWqxA25q0fai-twE6FbKS7lzJKGe643zA";
	private AudioManager audio;
	private Camera mCamera;
	private CameraPreview mPreview;
	protected static final String TAG = null;
	private static int SR_CODE = 123;
	public TextToSpeech tts;
	Typeface font;
	private Toast toast;
	private long lastBackPressTime = 0;
	Database database;
	Uri mImageCaptureUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_simple_layout);
		//WindowManager.LayoutParams lp = getWindow().getAttributes();
		//lp.screenBrightness = 0f;
		//getWindow().setAttributes(lp);
		initialize();

		//set background
		mCamera = getCameraInstance();
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);

		//enable drawing of box to select area of image
		OnTouchListener onTouchListner = new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub

				int action = event.getAction();

				x = (int) event.getX();
				y = (int) event.getY();

				switch (action) {

					case MotionEvent.ACTION_DOWN:
						startX = x;
						startY = y;
						break;
					case MotionEvent.ACTION_MOVE:
						DrawFocusRect(startX, startY, x, y);
						break;
					case MotionEvent.ACTION_UP:
						DrawFocusRect(startX, startY, x, y);
						//finalizeDrawing();
						break;
				}
				return true;
			}
		};
		preview.setOnTouchListener(onTouchListner);

		//set objects for image search invisible
		//tvObjectDetails.setVisibility(View.INVISIBLE);
		//tvProfile.setVisibility(View.INVISIBLE);
		tvInstructions.setVisibility(View.INVISIBLE);
		tvNotification.setVisibility(View.INVISIBLE);
		tvObject1.setVisibility(View.INVISIBLE);
		tvObject2.setVisibility(View.INVISIBLE);
		tvObject3.setVisibility(View.INVISIBLE);
		ivPreview.setVisibility(View.INVISIBLE);
		//ivPreviewProfile.setVisibility(View.INVISIBLE);
		//ivPreviewProfileCircle.setVisibility(View.INVISIBLE);
		//ivFriendlinessStars.setVisibility(View.INVISIBLE);
		//ivPlayfulnessStars.setVisibility(View.INVISIBLE);
		//tvProfileName.setVisibility(View.INVISIBLE);
		//tvProfileBackground.setVisibility(View.INVISIBLE);

		//set objects for text and voice search invisible
		bPlay.setVisibility(View.INVISIBLE);
		bSound.setVisibility(View.INVISIBLE);
		//bClose.setVisibility(View.INVISIBLE);
	}

	private void initialize() {
		// TODO Auto-generated method stub
		//set values for image search
		x = y = 0;
		//targetivHeight = targetivWidth = 0;
		//breedIndex = 0;
		//textOnClick = true;
		//imageOnClick = true;
		database = new Database(this);

		//set values for text and voice search
		myAudioToggle = true;

		//Define audio objects
		audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		tts = new TextToSpeech(this, this);

		//Define image objects
		ivPreview = (ImageView) findViewById(R.id.ivPreview);
		//ivPreviewProfile = (ImageView) findViewById(R.id.ivPreviewProfile);
		//ivPreviewProfileCircle = (ImageView) findViewById(R.id.ivPreviewProfileCircle);
		//ivFriendlinessStars = (ImageView) findViewById(R.id.ivFriendlinessStars);
		//ivPlayfulnessStars = (ImageView) findViewById(R.id.ivPlayfulnessStars);
		transparentView = (SurfaceView)findViewById(R.id.TransparentView);
		//transparentView.bringToFront();
		holderTransparent = transparentView.getHolder();
		holderTransparent.setFormat(PixelFormat.TRANSPARENT);
		holderTransparent.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		//Define textView objects
		//tvObjectDetails = (TextView) findViewById(R.id.tvObjectDetails);
		tvNotification = (TextView) findViewById(R.id.tvNotification);
		//tvNotification.bringToFront();
		//tvProfile = (TextView) findViewById(R.id.tvProfile);
		//tvProfileName = (TextView) findViewById(R.id.tvProfileName);
		//tvProfileBackground = (TextView) findViewById(R.id.tvProfileBackground);
		tvInstructions = (TextView) findViewById(R.id.tvInstructions);
		/*tvInstructions.setText(" Want to know what you are looking at? \n " +
				"		a. Point camera phone at object \n" +
				"		b. Draw box around dog (OPTIONAL) \n" +
				"		c. Click Search icon \n" +
				"										OR \n" +
				"		a. Upload an image \n");*/
		tvObject1 = (TextView) findViewById(R.id.tvObject1);
		tvObject2 = (TextView) findViewById(R.id.tvObject2);
		tvObject3 = (TextView) findViewById(R.id.tvObject3);

		//ivPreviewProfileCircle.setOnClickListener(this);
		//tvNotification.setOnClickListener(this);

		//Define buttons
		bSearch = (Button) findViewById(R.id.bSearch);
		bSound = (Button) findViewById(R.id.bMute);
		bUpload = (Button) findViewById(R.id.bUpload);
		bPlay = (Button) findViewById(R.id.bPlaySound);
		//bClose = (Button) findViewById(R.id.bClose);
		//bBrowse = (Button) findViewById(R.id.bBrowse);

		bSearch.setOnClickListener(this);
		bSound.setOnClickListener(this);
		bUpload.setOnClickListener(this);
		bPlay.setOnClickListener(this);
		//bClose.setOnClickListener(this);
		//bBrowse.setOnClickListener(this);

		//Define font
		font = Typeface.createFromAsset(getAssets(),"fonts/GoodDog.otf");
		tvNotification.setTypeface(font);
		tvObject1.setTypeface(font);
		//tvObject1.setTypeface(null, Typeface.BOLD);
		tvObject2.setTypeface(font);
		//tvObject2.setTypeface(null, Typeface.BOLD);
		tvObject3.setTypeface(font);
		//tvObject3.setTypeface(null, Typeface.BOLD);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		if (tts != null){
			tts.stop();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	public void onDestroy() {
		closeCamera();
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (tts != null) {
			tts.shutdown();
		}
		super.onDestroy();
	}

	/////////////////////START OF NON-MAIN FUNCTIONS///////////////////////////

	//BUTTON DEFINITION FUNCTIONS//

	public void onClick(View v){
		switch (v.getId()){
			//Toggles between mute and unmute
			/*case R.id.bMute:
				myAudioToggle = !myAudioToggle;
				setAudio(myAudioToggle);
				break;*/

			//Initiates gallery for user to select image from gallery
			case R.id.bUpload:
				x = y = 0;
				Clear();
				ivPreview.setVisibility(View.VISIBLE);
				startGalleryChooser();
				break;

			//Initiates capturing of picture or translation
			case R.id.bSearch:
				Clear();
				//If image search to initiate taking of picture
				ivPreview.setVisibility(View.VISIBLE);
				mCamera.takePicture(null, null, mPicture);
				mCamera.startPreview();
				/*Thread restart_preview=new Thread(){public void run(){
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					mCamera.release();
					mCamera=null;
					getCameraInstance();
				}};
				restart_preview.start();*/
				break;

			//Allows user to invoke reading breed name
			case R.id.bPlaySound:
				Speak();
				break;

			//Goes to browse activity
			/*case R.id.bBrowse:
				Intent intent = new Intent("com.my.digi.visionkids.BROWSE");
				this.startActivity(intent);
				break;*/
		}
	}

	//////////////////////////IMAGE PROCESSING FUNCTIONS/////////////////////////////

	/** A safe way to get an instance of the Camera object. */
	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		}
		catch (Exception e){
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	//draw box to isolate area of interest
	private void DrawFocusRect(int RectLeft, int RectTop, int RectRight, int RectBottom)
	{
		canvas = holderTransparent.lockCanvas();
		canvas.drawColor(0,Mode.CLEAR);
		//border's properties
		paint = new Paint();
		paint.setStyle(Paint.Style.STROKE);
		paint.setPathEffect(new DashPathEffect(new float[]{5, 10, 15, 20}, 0));
		paint.setColor(Color.RED);
		paint.setStrokeWidth(10);
		canvas.drawRect(RectLeft, RectTop, RectRight, RectBottom, paint);

		holderTransparent.unlockCanvasAndPost(canvas);
	}

	//on image taken
	PictureCallback mPicture = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			try {
				//compress image so that device does not freeze upon taking picture
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inTempStorage = new byte[16 * 1024];
				Camera.Parameters parameters = camera.getParameters();
				Camera.Size pictureSize = parameters.getPictureSize();

				int height11 = pictureSize.height;
				int width11 = pictureSize.width;
				float mb = (width11 * height11) / 1024000;

				if (mb > 4f)
					options.inSampleSize = 4;
				else if (mb > 3f)
					options.inSampleSize = 2;

				//Rotate and preview image
				Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				Matrix mat = new Matrix();
				mat.postRotate(90);  // angle is the desired angle you wish to rotate
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);

				//crop image based on box drawn
				if(x!=0 || y!=0){
					Display display = getWindowManager().getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					width = size.x;
					height = size.y;

					if (startX < x) {
						scaleX = (float) startX / (float) width * (float) bitmap.getWidth();
						scaleWidth = (float) (x - startX) / (float) width * (float) bitmap.getWidth();
					}
					else {
						scaleX = (float) x / (float) width * (float) bitmap.getWidth();
						scaleWidth = (float) (startX - x) / (float) width * (float) bitmap.getWidth();
					}

					if (startY < y){
						scaleY = (float) startY / (float) height* (float) bitmap.getHeight();
						scaleHeight = (float) (y-startY)/ (float) height* (float) bitmap.getHeight();
					}
					else{
						scaleY = (float) y / (float) height* (float) bitmap.getHeight();
						scaleHeight = (float) (startY-y)/ (float) height* (float) bitmap.getHeight();
					}

					cropX = (int)scaleX;
					cropY = (int)scaleY+15;
					cropWidth = (int)scaleWidth;
					cropHeight = (int)scaleHeight;

					Matrix matrix = new Matrix();
					matrix.postScale(0.5f, 0.5f);
					bitmap = Bitmap.createBitmap(bitmap, cropX, cropY, cropWidth, cropHeight, matrix, true);
					x = y = 0;
				}

				//display image and call Google api
				ivPreview.setImageBitmap(bitmap);
				bitmap = resizeBitmap(bitmap);
				callCloudVision(bitmap);

			} catch (FileNotFoundException e) {
			} catch (IOException e) {
			}
		}
	};

	//open gallery for selection
	public void startGalleryChooser() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select a photo"),
				GALLERY_IMAGE_REQUEST);
	}

	//load image from phone gallery to passed on for image search
	public void uploadImage(Uri uri) {
		if (uri != null) {
			try {
				// scale the image to save on bandwidth
				Bitmap bitmapUpload = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

				ivPreview.setImageBitmap(bitmapUpload);
				bitmapUpload = resizeBitmap(bitmapUpload);
				callCloudVision(bitmapUpload);

			} catch (IOException e) {
				Log.d(TAG, "Image picking failed because " + e.getMessage());
				Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
			}
		} else {
			Log.d(TAG, "Image picker gave us a null image.");
			Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
		}
	}

	//resizing image before sending it to google for search
	public Bitmap resizeBitmap(Bitmap bitmap) {

		int maxDimension = 1024;
		int originalWidth = bitmap.getWidth();
		int originalHeight = bitmap.getHeight();
		int resizedWidth = maxDimension;
		int resizedHeight = maxDimension;

		if (originalHeight > originalWidth) {
			resizedHeight = maxDimension;
			resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
		} else if (originalWidth > originalHeight) {
			resizedWidth = maxDimension;
			resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
		} else if (originalHeight == originalWidth) {
			resizedHeight = maxDimension;
			resizedWidth = maxDimension;
		}
		return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
	}

	//////////////////////IMAGE SEARCH FUNCTIONS/////////////////////////////

	//Converting image, sending image too Google and parsing results to be printed in textViews
	private void callCloudVision(final Bitmap bitmap) throws IOException {
		// Do the real work in an async task, because we need to use the network anyway
		new AsyncTask<Object, Void, String>() {
			ProgressDialog progressDialog = new ProgressDialog(Capture.this);

			protected void onPreExecute() {
				super.onPreExecute();

				progressDialog.setTitle("Uploading image. Please wait...");
				progressDialog.show();
			}

			protected String doInBackground(Object... params) {
				try {
					HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
					JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

					Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
					builder.setVisionRequestInitializer(new
							VisionRequestInitializer(CLOUD_VISION_API_KEY));
					Vision vision = builder.build();

					BatchAnnotateImagesRequest batchAnnotateImagesRequest =
							new BatchAnnotateImagesRequest();
					batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
						AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

						// Add the image
						Image base64EncodedImage = new Image();
						// Convert the bitmap to a JPEG
						// Just in case it's a format that Android understands but Cloud Vision
						ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
						bitmap.compress(CompressFormat.JPEG, 90, byteArrayOutputStream);
						byte[] imageBytes = byteArrayOutputStream.toByteArray();

						// Base64 encode the JPEG
						base64EncodedImage.encodeContent(imageBytes);
						annotateImageRequest.setImage(base64EncodedImage);

						List<Feature> featureList = new ArrayList<>();

						Feature labelDetection = new Feature();
						labelDetection.setType("LABEL_DETECTION");
						labelDetection.setMaxResults(10);
						featureList.add(labelDetection);

						annotateImageRequest.setFeatures(featureList);

						// Add the list of one thing to the request
						add(annotateImageRequest);
					}});

					Vision.Images.Annotate annotateRequest =
							vision.images().annotate(batchAnnotateImagesRequest);
					// Due to a bug: requests to Vision API containing large images fail when GZipped.
					annotateRequest.setDisableGZipContent(true);
					Log.d(TAG, "created Cloud Vision request object, sending request");

					BatchAnnotateImagesResponse response = annotateRequest.execute();
					return convertResponseToString(response);

				} catch (GoogleJsonResponseException e) {
					Log.d(TAG, "failed to make API request because " + e.getContent());
				} catch (IOException e) {
					Log.d(TAG, "failed to make API request because of other IOException " +
							e.getMessage());
				}
				return "Cloud Vision API request failed. Check logs for details.";
			}

			protected void onPostExecute(String result) {
				progressDialog.dismiss();

				objectResults = "";
				//breedFound = false;
				//breedIndex = 0;
				int n = 0;
				if (objectsArray != null){
					printResultsArray = new String[objectsArray.length];
					objectResults = objectResults + getNow() + "\n";
					for (int i=0; i < objectsArray.length; i++){
						if (database.foundExclusion(objectsMidArray[i])){

						}
						else{
							objectResults = objectResults + objectsMidArray[i] + "," + objectsArray[i] + "\n";
							printResultsArray[n] = objectsArray[i];
							n++;
						}
					}
					objectResults = objectResults + "\n";

					if (printResultsArray[0] != null){
						tvObject1.setVisibility(View.VISIBLE);
						tvObject1.setText(" " + printResultsArray[0]);
						bPlay.setVisibility(View.VISIBLE);
					}
					else{
						tvNotification.setText("No Object Identified");
					}
					if (printResultsArray[1] != null){
						tvObject2.setVisibility(View.VISIBLE);
						tvObject2.setText(" " + printResultsArray[1]);
					}
					if (printResultsArray[2] != null){
						tvObject3.setVisibility(View.VISIBLE);
						tvObject3.setText(" " + printResultsArray[2]);
					}

					Speak();

					try {
						writeToFile(Environment.getExternalStorageDirectory().getPath(), "logfileVisionKids.txt", objectResults);
					} catch (IOException e) {
						e.printStackTrace();
					}

					objectsArray = null;
					objectsMidArray = null;
					objectsScoreArray = null;
				}
				else {
					tvNotification.setVisibility(View.VISIBLE);
					tvNotification.setText("Please check internet connection");
				}
			}
		}.execute();
		mCamera.startPreview();
	}

	//Massaging information returned from Google. Arrays built in this function are used to print information to textViews in CallCloudVision function
	private String convertResponseToString(BatchAnnotateImagesResponse response) {
		StringBuilder message = new StringBuilder("Results:\n\n");
		message.append("Objects found:\n");

		int i = 0;
		List<EntityAnnotation> labels = response.getResponses().get(0).getLabelAnnotations();

		if (labels != null) {
			objectsArray = new String[labels.size()];
			objectsMidArray = new String[labels.size()];
			objectsScoreArray = new float[labels.size()];
			for (EntityAnnotation label : labels) {
				objectsArray[i]=label.getDescription();
				objectsMidArray[i]=label.getMid();
				objectsScoreArray[i]=label.getScore();
				if (objectsScoreArray[i] > 0.5) {
					message.append(String.format(Locale.getDefault(), "%.2f", label.getScore(), "%s", label.getDescription()));
					message.append("\n");
				}
				i += 1;
			}
		} else {
			message.append("nothing\n");
		}

		return message.toString();
	}

	//////////////////////AUDIO PROCESSING FUNCTIONS/////////////////////////////

	//initializing text to speech function
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			if (tts.isLanguageAvailable(Locale.US) == TextToSpeech.LANG_AVAILABLE)
				tts.setLanguage(Locale.US);
		} else if (status == TextToSpeech.ERROR) {
			Toast.makeText(this, "Sorry! Text To Speech failed...",
					Toast.LENGTH_LONG).show();
		}
	}

	//setting audio to mute and unmute
	private void setAudio(boolean b) {
		if (b){
			AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
			bSound.setBackgroundResource(R.drawable.sound_icon_on);
			Toast.makeText(this, "Unmute", Toast.LENGTH_LONG).show();
		}
		else{
			AudioManager amanager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
			bSound.setBackgroundResource(R.drawable.sound_icon_off);
			Toast.makeText(this, "Mute", Toast.LENGTH_LONG).show();
		}
	}

	private void Speak(){
		//tts.speak(dogBreedArray[breedIndex].toString(), TextToSpeech.QUEUE_FLUSH, null);
		if (printResultsArray[2] != null)
			tts.speak("I found three objects: " + printResultsArray[0] + ", " + printResultsArray[1] + ", " + printResultsArray[2], TextToSpeech.QUEUE_FLUSH, null);
		else if (printResultsArray[1] != null)
			tts.speak("I found two objects: " + printResultsArray[0] + ", " + printResultsArray[1], TextToSpeech.QUEUE_FLUSH, null);
		else if (printResultsArray[0] != null)
			tts.speak("I found one object: " + printResultsArray[0], TextToSpeech.QUEUE_FLUSH, null);
		else
			tts.speak("I did not find any objects", TextToSpeech.QUEUE_FLUSH, null);
	}

	//////////////////////IMAGE PROCESSING FUNCTIONS/////////////////////////////

	//Calls upload image function for image search and prints result of speech to text to editText before calling translation function
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
			mImageCaptureUri = data.getData();
			Crop(mImageCaptureUri);
		}

		else if (requestCode == REQUEST_CROP_ICON && resultCode == RESULT_OK && data != null){
			if(data != null ) {
				// get the returned data
				Bundle extras = data.getExtras();
				// get the cropped bitmap
				Bitmap selectedBitmap = extras.getParcelable("data");
				String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), selectedBitmap, "Title", null);
				mImageCaptureUri = Uri.parse(path);
				uploadImage(mImageCaptureUri);
			}
		}
	}

	private void Crop(Uri picUri) {
		try {
			Intent cropIntent = new Intent("com.android.camera.action.CROP");
			// indicate image type and Uri
			cropIntent.setDataAndType(picUri, "image/*");
			// set crop properties here
			cropIntent.putExtra("crop", true);
			// indicate aspect of desired crop
			cropIntent.putExtra("aspectX", 1);
			cropIntent.putExtra("aspectY", 1);
			// indicate output X and Y
			cropIntent.putExtra("outputX", 128);
			cropIntent.putExtra("outputY", 128);
			// retrieve data on return
			cropIntent.putExtra("return-data", true);
			// start the activity - we handle returning in onActivityResult
			startActivityForResult(cropIntent, REQUEST_CROP_ICON);
		}
		// respond to users whose devices do not support the crop action
		catch (ActivityNotFoundException anfe) {
			// display an error message
			String errorMessage = "This device doesn't support the crop action!";
			Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
			toast.show();
			uploadImage(mImageCaptureUri);
		}

	}

	//////////////////////////DOG PROFILE WRITE & DISPLAY FUNCTIONS//////////////////////////////////

	//Shows profile
	/*public void ShowProfile(){
		//tvObjectDetails.setVisibility(View.VISIBLE);
		//transparentView.setVisibility(View.INVISIBLE);
		tvNotification.setVisibility(View.INVISIBLE);
		ivPreviewProfileCircle.setVisibility(View.INVISIBLE);
		ClearCanvas();

		tvProfile.setVisibility(View.VISIBLE);
		tvProfile.setMovementMethod(new ScrollingMovementMethod());
		tvProfileName.setVisibility(View.VISIBLE);
		tvProfileBackground.setVisibility(View.VISIBLE);
		ivPreviewProfile.setVisibility(View.VISIBLE);
		ivFriendlinessStars.setVisibility(View.VISIBLE);
		ivPlayfulnessStars.setVisibility(View.VISIBLE);
		bClose.setVisibility(View.VISIBLE);
		bPlay.setVisibility(View.VISIBLE);

		String PACKAGE_NAME = getApplicationContext().getPackageName();
		tvProfileName.setText(database.getBreed(breedIndex));
		tvProfile.setText(
				"Breed Category:    					   			  " + database.getCategory(breedIndex) + "\n" +
				"Lifespan:              				     		      " + database.getLifeSpan(breedIndex) + "\n" +
				"Friendliness: \n" +
				"Playfulness:" );

		ivFriendlinessStars.setImageResource(getResources().getIdentifier(PACKAGE_NAME+":drawable/img_" + database.getFriendliness(breedIndex) + "_star", null, null));
		ivPlayfulnessStars.setImageResource(getResources().getIdentifier(PACKAGE_NAME+":drawable/img_" + database.getPlayfulness(breedIndex) + "_star", null, null));

		int id = getResources().getIdentifier(PACKAGE_NAME+":drawable/" + database.getImageName(breedIndex), null, null);
		ivPreviewProfile.setImageResource(id);
	}*/

	public void writeToFile(String directory, String filename, String data ) throws IOException {
		File out;
		OutputStreamWriter outStreamWriter = null;
		FileOutputStream outStream = null;

		out = new File(new File(directory), filename);

		if ( out.exists() == false ){
			out.createNewFile();
		}

		outStream = new FileOutputStream(out, true) ;
		outStreamWriter = new OutputStreamWriter(outStream);

		outStreamWriter.append(data);
		outStreamWriter.flush();
	}

	public String getNow() {

		Time now = new Time();
		now.setToNow();
		String sTime = now.format("%Y%m%d %H:%M:%S");
		return sTime;
	}

	//////////////////////////CLEARING FUNCTIONS//////////////////////////////////

	//Clears objects when a button is pressed
	public void Clear(){
		//tvObjectDetails.setVisibility(View.INVISIBLE);
		tvNotification.setVisibility(View.INVISIBLE);
		tvObject1.setVisibility(View.INVISIBLE);
		tvObject2.setVisibility(View.INVISIBLE);
		tvObject3.setVisibility(View.INVISIBLE);
		//tvProfile.setVisibility(View.INVISIBLE);
		//tvProfileName.setVisibility(View.INVISIBLE);
		//tvProfileBackground.setVisibility(View.INVISIBLE);
		bPlay.setVisibility(View.INVISIBLE);
		//bClose.setVisibility(View.INVISIBLE);
		ivPreview.setVisibility(View.INVISIBLE);
		//ivPreviewProfile.setVisibility(View.INVISIBLE);
		//ivPreviewProfileCircle.setVisibility(View.INVISIBLE);
		//ivFriendlinessStars.setVisibility(View.INVISIBLE);
		//ivPlayfulnessStars.setVisibility(View.INVISIBLE);
		objectsArray = null;
		objectsMidArray = null;
		objectsScoreArray = null;
		printResultsArray = null;
		ClearCanvas();
	}

	//Clears box drawn
	private void ClearCanvas()
	{
		canvas = holderTransparent.lockCanvas();
		canvas.drawColor(0,Mode.CLEAR);
		holderTransparent.unlockCanvasAndPost(canvas);
	}

	public void closeCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.setPreviewCallback(null);
			mCamera.lock();
			mCamera.release();
			mCamera=null;
		}
	}

	//////////////////////////MAIN KEY BUTTON FUNCTIONS//////////////////////////////////

	//forces volume button to control volume of music instead of ringer
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_VOLUME_UP:
				audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
				return true;
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				audio.adjustStreamVolume(AudioManager.STREAM_MUSIC,
						AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
				return true;
			case KeyEvent.KEYCODE_BACK:
				if (this.lastBackPressTime < System.currentTimeMillis() - 4000) {
					toast = Toast.makeText(this, "Press back again to close this app", 4000);
					toast.show();
					this.lastBackPressTime = System.currentTimeMillis();
				} else {
					if (toast != null) {
						toast.cancel();
					}
					Intent intent = new Intent(Intent.ACTION_MAIN);
					intent.addCategory(Intent.CATEGORY_HOME);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(intent);
				}
			default:
				return false;
		}
	}
}

