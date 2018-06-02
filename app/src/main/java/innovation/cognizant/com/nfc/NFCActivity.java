package innovation.cognizant.com.nfc;

import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

public class NFCActivity extends AppCompatActivity implements
        NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback {

    private static final String TAG = "NFCActivity" ;
    EditText mInputView;
    private NfcAdapter mNfcAdapter;
    private LinearLayout receivedDataLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        init();
    }

    private void init() {
        mInputView = (EditText) findViewById(R.id.input_text);
        receivedDataLayout = (LinearLayout) findViewById(R.id.data_layout);
        initNfcAdapter();
        if (getIntent().getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)) {
            handleNfcIntent(getIntent());
        }
    }

    private void handleNfcIntent(Intent nfcIntent) {
        if(nfcIntent!=null && nfcIntent.getAction().equals(NfcAdapter.ACTION_NDEF_DISCOVERED)){
            Parcelable[] receivedPayload =  nfcIntent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (receivedPayload!=null) {
                NdefMessage receivedMessage = (NdefMessage) receivedPayload[0];
                NdefRecord[] receivedRecord = receivedMessage.getRecords();
                Log.i(TAG, "receivedRecord Size::"+receivedRecord.length);
                byte[] decodedBytes = Base64.decode(receivedRecord[0].getPayload(),Base64.DEFAULT);
                String payload = new String(decodedBytes);
                Log.i(TAG,"Received payload::"+payload );
                Toast.makeText(this, "Received Data::: "+payload, Toast.LENGTH_LONG).show();
                attachDataToLayout(payload);
                //TODO: Need to reset intent?? to handle orientation??
            }
        }

    }

    private void attachDataToLayout(final String payload) {

        Log.i(TAG,"is receivedDataLayout NULL??::"+receivedDataLayout );
        if(receivedDataLayout == null) {
            return;
        }

        TextView textview = new TextView(this);
        textview.setText(payload);
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        textLayoutParams.setMargins(10, 0,0,10);
        textview.setLayoutParams(textLayoutParams);
        receivedDataLayout.addView(textview);
        Log.i(TAG,"receivedDataLayout ChildCount??::"+receivedDataLayout.getChildCount() );
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleNfcIntent(intent);

    }

    private void initNfcAdapter() {

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if(mNfcAdapter !=null ) {
            mNfcAdapter.setNdefPushMessageCallback(this, this);

            mNfcAdapter.setOnNdefPushCompleteCallback(this,this);
        }
        else{
            Toast.makeText(this, "NFC not available on this device",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {

        //get input string/text
        String textToSend = mInputView.getText().toString();
        if(TextUtils.isEmpty(textToSend)) {
            return null;
        }

       // NdefRecord ndfRecord = createNdefRecord(textToSend);
        //NdefRecord record = null;

        byte[] data = textToSend.getBytes(Charset.defaultCharset());
        byte[] base64Payload = Base64.encode(data, Base64.DEFAULT);

        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { NdefRecord.createMime(
                        "application/"+getPackageName(), base64Payload)
                        ,NdefRecord.createApplicationRecord(getPackageName())});

        //NdefRecord.createApplicationRecord("innovation.cognizant.com.nfc")

        return msg;
    }

    private NdefRecord createNdefRecord(String textToSend) {
        NdefRecord record = null;
        byte[] data = textToSend.getBytes(Charset.defaultCharset());
        byte[] base64Payload = Base64.encode(data, Base64.DEFAULT);


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
             record = new NdefRecord(NdefRecord.TNF_WELL_KNOWN
            , NdefRecord.RTD_TEXT, new byte[0], base64Payload);

        } else {
            record = NdefRecord.createMime("text/plain",base64Payload);
        }
        Log.i(TAG, "NdefRecord::"+record);
        return record;
    }

    @Override
    public void onNdefPushComplete(NfcEvent event) {

        //Toast.makeText(this, "Message sent successfully", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Message sent successfully::");
        if(mInputView!=null) {
            //reset input box
            try{
                mInputView.setText("");
            }catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }
}
