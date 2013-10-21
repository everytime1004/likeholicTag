package com.torykorea.likeholictagrw;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	boolean mWriteMode = false;
	boolean mReadMode = false;
	private NfcAdapter mNfcAdapter;
	private PendingIntent mNfcPendingIntent;
	private AlertDialog.Builder alertDialog;
	private AlertDialog resultDialog;
	private final String IP = "http://14.63.168.158";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		((TextView) findViewById(R.id.pidEt))
				.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						// TODO Auto-generated method stub
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
						// TODO Auto-generated method stub

					}

					@Override
					public void afterTextChanged(Editable s) {
						// TODO Auto-generated method stub
						// Log.d("test", String.valueOf(s.length()));
						if (s.length() == 5) {
							try {
								requestPage();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				});

		((Button) findViewById(R.id.tagWriteBtn))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mNfcAdapter = NfcAdapter
								.getDefaultAdapter(MainActivity.this);
						mNfcPendingIntent = PendingIntent.getActivity(
								MainActivity.this,
								0,
								new Intent(MainActivity.this,
										MainActivity.class)
										.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
								0);

						enableTagMode(1);

						alertDialog = new AlertDialog.Builder(MainActivity.this);
						alertDialog.setTitle("태그를 가까이하세요.(쓰기 모드!)");
						alertDialog
								.setOnCancelListener(new DialogInterface.OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										disableTagWriteMode();
									}
								});
						resultDialog = alertDialog.show();
					}
				});
		((Button) findViewById(R.id.tagReadBtn))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mNfcAdapter = NfcAdapter
								.getDefaultAdapter(MainActivity.this);
						mNfcPendingIntent = PendingIntent.getActivity(
								MainActivity.this,
								0,
								new Intent(MainActivity.this,
										MainActivity.class)
										.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
								0);

						enableTagMode(2);

						alertDialog = new AlertDialog.Builder(MainActivity.this);
						alertDialog.setTitle("태그를 가까이하세요.(읽기 모드!)");
						alertDialog
								.setOnCancelListener(new DialogInterface.OnCancelListener() {
									@Override
									public void onCancel(DialogInterface dialog) {
										disableTagWriteMode();
									}
								});
						resultDialog = alertDialog.show();
					}
				});
	}

	/**
	 * @param which
	 *            1 -> write, 2 -> read
	 */
	private void enableTagMode(int which) {
		if (which == 1) {
			mWriteMode = true;
		} else {
			mReadMode = true;
		}
		IntentFilter tagDetected = new IntentFilter(
		// NfcAdapter.ACTION_TECH_DISCOVERED);
				NfcAdapter.ACTION_TAG_DISCOVERED);
		IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
				mWriteTagFilters, null);
	}

	private void disableTagWriteMode() {
		mWriteMode = false;
		mReadMode = false;
		mNfcAdapter.disableForegroundDispatch(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Tag writing mode
		if (mWriteMode
				&& NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			// 현재 개발중인거
			byte[] one = "pid=".getBytes();
			byte[] two = ((TextView) findViewById(R.id.pidEt)).getText()
					.toString().getBytes();
			byte[] three = "?index=".getBytes();
			byte[] four;
			if (((TextView) findViewById(R.id.indexEt)).getText().toString()
					.equals("")) {
				four = "1".getBytes();
			} else {
				four = ((TextView) findViewById(R.id.indexEt)).getText()
						.toString().getBytes();
			}

			byte[] five = "?name=".getBytes();
			byte[] six = ((TextView) findViewById(R.id.shopNameEt)).getText()
					.toString().getBytes();
			byte[] combinedData = new byte[one.length + two.length
					+ three.length + four.length + five.length + six.length];

			System.arraycopy(one, 0, combinedData, 0, one.length);
			System.arraycopy(two, 0, combinedData, one.length, two.length);
			System.arraycopy(three, 0, combinedData, one.length + two.length,
					three.length);
			System.arraycopy(four, 0, combinedData, one.length + two.length
					+ three.length, four.length);
			System.arraycopy(five, 0, combinedData, one.length + two.length
					+ three.length + four.length, five.length);
			System.arraycopy(six, 0, combinedData, one.length + two.length
					+ three.length + four.length + five.length, six.length);

			// 여기는 기존 likeholic
			// byte[] version = { 0x01, 0x01, 0x33 };
			// byte[] pid = ((TextView) findViewById(R.id.pidEt)).getText()
			// .toString().getBytes();
			// byte[] pidform = { 0x02, (byte) pid.length };
			// byte[] index = ((TextView) findViewById(R.id.indexEt)).getText()
			// .toString().getBytes();
			// byte[] indexform = { 0x03, (byte) index.length };
			// byte[] end = { (byte) 0xFE };
			//
			// byte[] combinedData = new byte[index.length + pid.length + 8];
			//
			// System.arraycopy(version, 0, combinedData, 0, version.length);
			// System.arraycopy(pidform, 0, combinedData, version.length,
			// pidform.length);
			// System.arraycopy(pid, 0, combinedData, version.length
			// + pidform.length, pid.length);
			// System.arraycopy(indexform, 0, combinedData, version.length
			// + pidform.length + pid.length, indexform.length);
			// System.arraycopy(index, 0, combinedData, version.length
			// + pidform.length + pid.length + indexform.length,
			// index.length);
			// System.arraycopy(end, 0, combinedData, version.length
			// + pidform.length + pid.length + indexform.length
			// + index.length, end.length);

			// byte[] uriField =
			// "http://market.android.com/details?id=com.torykorea.likeholic"
			// .getBytes(Charset.forName("US-ASCII"));
			byte[] uriField = "market://details?id=com.torykorea.likeholic"
					.getBytes(Charset.forName("US-ASCII"));
			byte[] payload = new byte[uriField.length + 1]; // add 1 for the

			// System.arraycopy(uriField, 0, payload, 1, uriField.length);
			// // appends URI to payload
			// NdefRecord uriRecord = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
			// NdefRecord.RTD_URI, new byte[0], payload);
			//
			// // NdefRecord idRecord = NdefRecord.createMime(
			// // "text/plain", combinedData);
			// NdefMessage idRecord = new NdefMessage(
			// new NdefRecord[] { NdefRecord
			// .createMime("application/com.torykorea.likeholic",
			// combinedData)

			// });

			NdefMessage message = new NdefMessage(
					new NdefRecord[] {
							NdefRecord.createMime("application/likeholic",
									combinedData)
							/**
							 * The Android Application Record (AAR) is commented
							 * out. When a device receives a push with an AAR in
							 * it, the application specified in the AAR is
							 * guaranteed to run. The AAR overrides the tag
							 * dispatch system. You can add it back in to
							 * guarantee that this activity starts when
							 * receiving a beamed message. For now, this code
							 * uses the tag dispatch system.
							 */
							,
							NdefRecord
									.createApplicationRecord("com.torykorea.likeholic") });

			// NdefMessage message = new NdefMessage(new NdefRecord[] {
			// NdefRecord.createMime(
			// "application/com.torykorea.likeholic", combinedData ) });

			if (writeTag(message, detectedTag)) {
				Toast.makeText(this, "Success: Write id to nfc tag",
						Toast.LENGTH_SHORT).show();
			}

		} else if (mReadMode
				&& NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {

			try {
				NdefMessage m = (NdefMessage) intent
						.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)[0];
				if (m != null) {
					NdefRecord r = null;
					byte[] aa = "application/likeholic".getBytes();
					byte[] type;
					for (int i = 0; i < m.getRecords().length; i++) {
						type = m.getRecords()[i].getType();
						boolean chk = true;
						for (int j = 0; j < type.length; j++) {
							if (type[j] != aa[j]) {
								chk = false;
								break;
							}
						}
						if (chk) {
							r = m.getRecords()[i];
							break;
						}
					}

					if (r == null) {
						Toast.makeText(getApplicationContext(),
								"우리 서비스 태그가 아닙니다.", Toast.LENGTH_SHORT).show();
						// 지원하지 않는 태그
					} else {
						// 우리서비스
						byte[] detectedPayload = r.getPayload();

						String detectedPayloadString = new String(
								detectedPayload);

						try {
							String idPart = detectedPayloadString.split("\\?")[0]
									.split("\\=")[1];
							String indexPart = detectedPayloadString
									.split("\\?")[1].split("\\=")[1];
							String namePart = detectedPayloadString
									.split("\\?")[2].split("\\=")[1];
							String[] tagInfo = { idPart, indexPart, namePart };

							((TextView) findViewById(R.id.pidEt))
									.setText(idPart);
							((TextView) findViewById(R.id.indexEt))
									.setText(indexPart);
							((TextView) findViewById(R.id.shopNameEt))
									.setText(namePart);
							resultDialog.dismiss();

						} catch (Exception e) {
							// TODO: handle exception
							Toast.makeText(getApplicationContext(),
									"우리 서비스 태그가 아닙니다.", Toast.LENGTH_SHORT)
									.show();
						}

					}
				}

			} catch (Exception e) {
				Toast.makeText(getApplicationContext(), "우리 서비스 태그가 아닙니다.",
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	/*
	 * Writes an NdefMessage to a NFC tag
	 */
	public boolean writeTag(NdefMessage message, Tag tag) {

		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				if (!ndef.isWritable()) {
					Toast.makeText(getApplicationContext(),
							"Error: tag not writable", Toast.LENGTH_SHORT)
							.show();
					return false;
				}
				if (ndef.getMaxSize() < 100) {
					Toast.makeText(getApplicationContext(),
							"Error: tag too small", Toast.LENGTH_SHORT).show();
					return false;
				}
				ndef.writeNdefMessage(message);
				// ndef.makeReadOnly();
				return true;
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						return true;
					} catch (IOException e) {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	AsyncTask<String, Void, JSONObject> requestTask;

	private void requestPage() throws IOException {

		requestTask = new AsyncTask<String, Void, JSONObject>() {

			@Override
			protected JSONObject doInBackground(String... urls) {
				DefaultHttpClient client = new DefaultHttpClient();

				HttpGet get = new HttpGet(urls[0]);
				String response = null;
				JSONObject json = new JSONObject();

				try {
					try {
						json.put("success", false);
						json.put("info", "Something went wrong. Retry!");

						get.setHeader("Accept", "application/json");
						get.setHeader("Content-Type", "application/json");

						ResponseHandler<String> responseHandler = new BasicResponseHandler();
						response = client.execute(get, responseHandler);
						json = new JSONObject(response);

					} catch (HttpResponseException e) {
						e.printStackTrace();
						Log.e("ClientProtocol", "" + e);
					} catch (IOException e) {
						e.printStackTrace();
						Log.e("IO", "" + e);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					Log.e("JSON", "" + e);
				}

				return json;
			}

			@Override
			protected void onPostExecute(JSONObject json) {
				try {
					if (json.getBoolean("success")) {
						((TextView) findViewById(R.id.shopNameEt)).setText(json
								.getJSONObject("data").getString("page_name"));
					}
				} catch (Exception e) {
					try {
						Toast.makeText(getApplicationContext(),
								json.getString("info"), Toast.LENGTH_LONG)
								.show();
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} finally {
					super.onPostExecute(json);
				}
			}

		};

		TextView pid = (TextView) findViewById(R.id.pidEt);
		String url = IP + "/api/v1/taggedRW?pid=" + pid.getText().toString();
		// 유저 reg_id update 하는 URL
		requestTask.execute(url);
	}
}
