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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.Charset;

public class NFCActivity extends AppCompatActivity implements
        NfcAdapter.OnNdefPushCompleteCallback, NfcAdapter.CreateNdefMessageCallback {

    private static final String TAG = "NFCActivity" ;
    EditText mInputView;
    Button sendButton;
    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        init();
    }

    private void init() {
        mInputView = (EditText) findViewById(R.id.input_text);
        sendButton = (Button) findViewById(R.id.send_btn);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

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
                byte[] decodedBytes = Base64.decode(receivedRecord[0].getPayload(),Base64.DEFAULT);
                String payload = new String(decodedBytes);
                Log.i(TAG,"Received payload::"+payload );
                Toast.makeText(this, "Received Data::: "+payload, Toast.LENGTH_LONG).show();
                //TODO: Need to reset intent?? to handle orientation??
            }
        }

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

        NdefRecord ndfRecord = createNdefRecord(textToSend);
        return new NdefMessage(ndfRecord);
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
            mInputView.setText("");
        }
    }
}
