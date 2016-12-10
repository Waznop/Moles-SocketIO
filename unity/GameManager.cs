using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class GameManager : MonoBehaviour {

	public float startDelay = 2f;
	public static GameManager instance = null;
	public BoardManager boardScript;
	private Text molesText;
	private GameObject gameImage;
	private int round = 0;

	private Text moleScore;

	void Awake() {
		if (instance == null)
			instance = this;
		else if (instance != this)
			Destroy (gameObject);
		DontDestroyOnLoad (gameObject);

		boardScript = GetComponent<BoardManager> ();

		InitGame ();

	}

	private void OnLevelWasLoaded (int index) {
		round++;
		InitGame ();
	}

	void InitGame() {

		Variables.pause = false;
		gameImage = GameObject.Find ("GameImage");
		molesText = GameObject.Find ("MolesText").GetComponent<Text> ();
		molesText.text = "Moles - Round " + round;
		moleScore = GameObject.Find ("MoleScore").GetComponent<Text> ();
		moleScore.text = Variables.p1Score + " - " + Variables.p2Score;
		gameImage.SetActive (true);
		Invoke ("HideGameImage", startDelay);
		boardScript.SetupScene ();
	}

	private void HideGameImage() {
		gameImage.SetActive (false);
	}

	public void GameOver() {
		molesText.text = "Thank you come again!";
		gameImage.SetActive (true);
		enabled = false;
	}

	void Update() {
		
	}

}
