using UnityEngine;
using System.Collections;

public class BGM : MonoBehaviour {

	public AudioSource efxSource2;
	public AudioSource musicSource2;
	public static BGM instance = null;

	void Awake () {
		if (instance == null)
			instance = this;
		else if (instance != this)
			Destroy (gameObject);

		DontDestroyOnLoad (gameObject);
	}

	public void PlaySingle(AudioClip clip) {
		efxSource2.clip = clip;
		efxSource2.Play ();
	}

}
