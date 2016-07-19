package com.example.jeffery.blackjack;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends ActionBarActivity {

    private int playerCash;
    private int playerBet;
    private boolean playing, playerStands, dealerStands, shoeMode;

    TextView totalCashTextView, playerBetTextView, playerScoreTextView, dealerScoreTextView, winnerTextView;

    Button betPlusButton, betMinusButton, shoeModeButton, hitButton, standButton, newGameButton;

    ImageView playerCard1, playerCard2, playerCard3, playerCard4, playerCard5;
    ImageView dealerCard1, dealerCard2, dealerCard3, dealerCard4, dealerCard5;

    ImageView playerCardImages[] = {playerCard1, playerCard2, playerCard3, playerCard4, playerCard5};
    ImageView dealerCardImages[] = {dealerCard1, dealerCard2, dealerCard3, dealerCard4, dealerCard5};

    hand playerCards;
    hand dealerCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        playing = false;
        shoeMode = true;
        playerStands = false;
        dealerStands = false;
        playerCash = 1000;
        playerBet = 100;

        totalCashTextView = (TextView) findViewById(R.id.totalCashText);
        playerBetTextView = (TextView) findViewById(R.id.currentBetText);
        playerScoreTextView = (TextView) findViewById(R.id.playerScoreText);
        dealerScoreTextView = (TextView) findViewById(R.id.dealerScoreText);
        winnerTextView = (TextView) findViewById(R.id.winnerTextView);

        betPlusButton = (Button) findViewById(R.id.betPlusButton);
        betMinusButton = (Button) findViewById(R.id.betMinusButton);
        shoeModeButton = (Button) findViewById(R.id.shoeModeButton);
        hitButton = (Button) findViewById(R.id.hitButton);
        standButton = (Button) findViewById(R.id.standButton);
        newGameButton = (Button) findViewById(R.id.newGameButton);

        setButtonOnClickListeners();

        playerCardImages[0] = (ImageView) findViewById(R.id.imagePlayerCard1);
        playerCardImages[1] = (ImageView) findViewById(R.id.imagePlayerCard2);
        playerCardImages[2] = (ImageView) findViewById(R.id.imagePlayerCard3);
        playerCardImages[3] = (ImageView) findViewById(R.id.imagePlayerCard4);
        playerCardImages[4] = (ImageView) findViewById(R.id.imagePlayerCard5);

        dealerCardImages[0] = (ImageView) findViewById(R.id.imageDealerCard1);
        dealerCardImages[1] = (ImageView) findViewById(R.id.imageDealerCard2);
        dealerCardImages[2] = (ImageView) findViewById(R.id.imageDealerCard3);
        dealerCardImages[3] = (ImageView) findViewById(R.id.imageDealerCard4);
        dealerCardImages[4] = (ImageView) findViewById(R.id.imageDealerCard5);

        newGame();
    }

    private void newGame() {

        playing = true;
        playerStands = false;
        dealerStands = false;

        playerCards = new hand();
        dealerCards = new hand();

        winnerTextView.setVisibility(View.INVISIBLE);
        newGameButton.setVisibility(View.INVISIBLE);

        for (ImageView playerCardImage : playerCardImages) {
            playerCardImage.setImageResource(R.drawable.blank);
            playerCardImage.setVisibility(View.INVISIBLE);
        }

        for (ImageView dealerCardImage : dealerCardImages) {
            dealerCardImage.setImageResource(R.drawable.blank);
            dealerCardImage.setVisibility(View.INVISIBLE);
        }

        dealCard(playerCards);
        dealCard(dealerCards);
        dealCard(playerCards);
        dealCard(dealerCards);

        updateCards();
    }

    private void updateCards(){
        for (int i = 0; i < playerCards.cards.size(); i++){
            String s = ("card_" + playerCards.cards.get(i).number + "_of_" + playerCards.cards.get(i).suit);
            int id = getResources().getIdentifier(s, "drawable", getPackageName());
            playerCardImages[i].setVisibility(View.VISIBLE);
            playerCardImages[i].setImageResource(id);
        }
        for (int i = 0; i < dealerCards.cards.size(); i++){
            String s = ("card_" + dealerCards.cards.get(i).number + "_of_" + dealerCards.cards.get(i).suit);
            int id = getResources().getIdentifier(s, "drawable", getPackageName());
            dealerCardImages[i].setVisibility(View.VISIBLE);
            dealerCardImages[i].setImageResource(id);
        }

        totalCashTextView.setText(getResources().getString(R.string.total_cash) + playerCash);
        playerBetTextView.setText(getResources().getString(R.string.current_bet) + playerBet);
        playerScoreTextView.setText(getResources().getString(R.string.player_score) + playerCards.score);
        dealerScoreTextView.setText(getResources().getString(R.string.dealer_score) + dealerCards.score);

    }

    private void setButtonOnClickListeners() {

        betPlusButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!playing){
                    playerBet += 10;
                    updateCards();
                }
            }
        });

        betMinusButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!playing){
                    if (playerBet != 0){
                        playerBet -= 10;
                    }
                    updateCards();
                }
            }
        });

        shoeModeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (!playing){
                    if (shoeMode){
                        shoeMode = false;
                        shoeModeButton.setText(R.string.shoe_mode_off);
                    }else{
                        shoeMode = true;
                        shoeModeButton.setText(R.string.shoe_mode_on);
                    }
                }
            }
        });

        hitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (playing){
                    dealCard(playerCards);
                    updateCards();
                    if (playerCards.score > 21){
                        if (playerCards.aces > 0){
                            playerCards.decAces();
                            playerCards.decScore();
                            updateCards();
                        }else{
                            dealerWins();
                        }
                    }
                    if (playerCards.cards.size() >= 5 && playerCards.score <= 21){
                        playerWins();
                    }
                    dealerTurn();
                }
            }
        });

        standButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (playing){
                    playerStands = true;
                    if (dealerStands){
                        scoreCompare();
                    }
                    dealerTurn();
                }
            }
        });

        newGameButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                newGame();
            }
        });
    }

    private void scoreCompare() {
        updateCards();
        if (playerCards.score > 21){
            dealerWins();
        }else if (dealerCards.score > 21){
            playerWins();
        }else if (playerCards.score > dealerCards.score){
            playerWins();
        }else{
            dealerWins();
        }
    }

    private void dealerWins() {
        playerCash -= playerBet;
        playing = false;
        updateCards();
        winnerTextView.setText(R.string.dealer_wins);
        winnerTextView.setVisibility(View.VISIBLE);
        newGameButton.setVisibility(View.VISIBLE);
    }

    private void playerWins(){
        playerCash += playerBet;
        playing = false;
        updateCards();
        winnerTextView.setText(R.string.player_wins);
        winnerTextView.setVisibility(View.VISIBLE);
        newGameButton.setVisibility(View.VISIBLE);
    }

    private void dealerTurn(){
        if (playing){
            if (dealerCards.score < 17){
                dealCard(dealerCards);
                updateCards();
                if (dealerCards.score > 21){
                    if (dealerCards.aces > 0){
                        dealerCards.decAces();
                        dealerCards.decScore();
                        updateCards();
                    }else{
                        playerWins();
                    }
                }
                if (dealerCards.cards.size() >= 5 && dealerCards.score <= 21){
                    dealerWins();
                }
            }else{
                dealerStands = true;
                if (playerStands){
                    scoreCompare();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class card{
        private String suit;
        private String number;

        public card(){
            this.suit = null;
            this.number = null;
        }

        public void setSuit(String newSuit){
            this.suit = newSuit;
        }

        public String getSuit(){
            return suit;
        }

        public void setNumber(String newNumber){
            this.number = newNumber;
        }

        public String getNumber(){
            return number;
        }
    }

    class hand{
        private int aces = 0;
        private int score = 0;
        private ArrayList<card> cards = new ArrayList<>();

        public void incAces(){
            this.aces++;
        }

        public void decAces(){
            this.aces--;
        }

        public void incScore(int scorePlus){
            this.score += scorePlus;
        }

        public void decScore(){
            this.score = this.score - 10;
        }

    }

    public void dealCard(hand hand){
        boolean repeatCheck = true;
        card nextCard = new card();

        if (!shoeMode){
            while (repeatCheck){
                generateCard(nextCard);
                if (!(repeatCard(nextCard.getNumber(), nextCard.getSuit()))){
                    repeatCheck = false;
                }
            }
        }else {
            generateCard(nextCard);
        }

        if (nextCard.number.equals("ace")){
            hand.incAces();
        }

        hand.cards.add(nextCard);
        hand.incScore(faceValue(nextCard));
        updateCards();
    }

    public void generateCard(card nextCard){
        int dealSuit;
        int dealNumber;
        String checkSuit;
        String checkNumber;

        Random r = new Random();
        dealSuit = r.nextInt(4) + 1;

        switch (dealSuit){
            case 1:
                checkSuit = "hearts";
                break;
            case 2:
                checkSuit = "clubs";
                break;
            case 3:
                checkSuit = "diamonds";
                break;
            default:
                checkSuit = "spades";
                break;
        }

        dealNumber = r.nextInt(13) + 1;

        switch (dealNumber){
            case 1:
                checkNumber = "ace";
                break;
            case 11:
                checkNumber = "jack";
                break;
            case 12:
                checkNumber = "queen";
                break;
            case 13:
                checkNumber = "king";
                break;
            default:
                checkNumber = Integer.toString(dealNumber);
                break;
        }

        nextCard.setSuit(checkSuit);
        nextCard.setNumber(checkNumber);
    }

    public int faceValue(card card){
        switch (card.number){
            case "2":
                return 2;
            case "3":
                return 3;
            case "4":
                return 4;
            case "5":
                return 5;
            case "6":
                return 6;
            case "7":
                return 7;
            case "8":
                return 8;
            case "9":
                return 9;
            case "10":
                return 10;
            case "jack":
                return 10;
            case "queen":
                return 10;
            case "king":
                return 10;
            case "ace":
                return 11;
            default:
                return 100;
        }
    }

    public boolean repeatCard(String number, String suit){
        for (int i = 0; i < playerCards.cards.size(); i++){
            if ((suit.equals(playerCards.cards.get(i).suit))&&(number.equals(playerCards.cards.get(i).number))){
                return true;
            }
        }
        for (int i = 0; i < dealerCards.cards.size(); i++){
            if ((suit.equals(dealerCards.cards.get(i).suit))&&(number.equals(dealerCards.cards.get(i).number))){
                return true;
            }
        }

        return false;
    }
}
