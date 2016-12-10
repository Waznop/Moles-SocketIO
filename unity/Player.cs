using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class Player : MonoBehaviour {

	public int id = 1;
	public static float energyCap = 10f;
	public float energy = energyCap;
	public float fatigue = 10f;
	public float regen = 10f;
	public bool gameOver = false;
	public bool under = false;
	public bool popping = false;
	public bool facingRight = true;
	public float restartDelay = 2f;
	private Animator animator;
	public Text mole1Energy;
	public Text mole2Energy;
	public float speed = 7.5f;
	Vector3 pos;
	private Rigidbody2D myRigidBody;
	public LayerMask solid;
	public LayerMask bound;
	private bool p1win = false;
	public AudioClip popSound;
	public AudioClip digSound;
	public AudioClip winSound;

	void Start() {
		animator = GetComponent<Animator> ();
		myRigidBody = GetComponent<Rigidbody2D>();
		pos = myRigidBody.position;
	}

	private bool checkCollision (int horizontal, int vertical) {

		Vector2 start = transform.position;
		Vector2 end = start + new Vector2 (horizontal, vertical);
		RaycastHit2D hit = Physics2D.Linecast (start, end, solid);
		RaycastHit2D hit2 = Physics2D.Linecast (start, end, bound);
		if (hit2.transform == null && (hit.transform == null || under)) {
			return true;
		} else {
			return false;
		}
	}

	private bool checkReady () {
		return (myRigidBody.position.x == 0 && myRigidBody.position.y == 0) ||
			(myRigidBody.position.x % 1 == 0 && myRigidBody.position.y % 1 == 0);
	}

	void FixedUpdate() {

		if (popping)
			popping = false;
		
		if (Variables.pause) {
			return;
		}

		if (id == 1) {

			if (Input.GetKey (KeyCode.A) && transform.position == pos) {
				if (checkCollision (-1, 0) && checkReady())
					pos += Vector3.left;
				if (facingRight) {
					animator.transform.Rotate (0, 180, 0);
					facingRight = false;
				}
					
			}	
			if (Input.GetKey (KeyCode.D) && transform.position == pos) {
				if (checkCollision (1, 0) && checkReady())
					pos += Vector3.right;
				if (!facingRight) {
					animator.transform.Rotate (0, 180, 0);
					facingRight = true;
				}
			}
			if (Input.GetKey (KeyCode.W) && transform.position == pos) {
				if (checkCollision (0, 1) && checkReady())
					pos += Vector3.up;
			}
			if (Input.GetKey (KeyCode.S) && transform.position == pos) {
				if (checkCollision (0, -1) && checkReady())
					pos += Vector3.down;
			}
			myRigidBody.position = Vector3.MoveTowards (transform.position, pos, Time.deltaTime * speed);

			if (Input.GetKeyDown (KeyCode.E)) {
				if (under && checkReady()) {
					pop ();
				} else if (checkReady()) {

			        SoundManager.instance.PlaySingle (digSound);
					animator.SetTrigger ("playerDig");
					under = true;
				}
			}
		}
			
		if (id == 2) {
			if(Input.GetKey(KeyCode.LeftArrow) && transform.position == pos) {
				if (checkCollision (-1, 0))
					pos += Vector3.left;
				if (facingRight) {
					animator.transform.Rotate (0, 180, 0);
					facingRight = false;
				}
			}	
			if(Input.GetKey(KeyCode.RightArrow) && transform.position == pos) {
				if (checkCollision (1, 0))
					pos += Vector3.right;
				if (!facingRight) {
					animator.transform.Rotate (0, 180, 0);
					facingRight = true;
				}
			}
			if(Input.GetKey(KeyCode.UpArrow) && transform.position == pos) {
				if (checkCollision (0, 1))
					pos += Vector3.up;
			}
			if(Input.GetKey(KeyCode.DownArrow) && transform.position == pos) {
				if (checkCollision (0, -1))
					pos += Vector3.down;
			}
			myRigidBody.position = Vector3.MoveTowards(transform.position, pos, Time.deltaTime * speed);

			if (Input.GetKeyDown (KeyCode.RightShift)) {
				if (under) {
					pop ();
				} else {
					SoundManager.instance.PlaySingle (digSound);
					animator.SetTrigger ("playerDig");
					under = true;
				}
			}
		}

		if (energy == 0 && under)
			pop ();

		if (under && energy > 0)
			energy -= fatigue * Time.deltaTime;
		else if (!under && energy < energyCap)
			energy += regen * Time.deltaTime;
		if (energy < 0)
			energy = 0;
		else if (energy > energyCap)
			energy = energyCap;

		if (id == 1)
			mole1Energy.text = "Mole 1 Energy: " + (int)energy * 10 + "%";
		else if (id == 2)
			mole2Energy.text = "Mole 2 Energy: " + (int)energy * 10 + "%";

	}

	private void pop() {
		SoundManager.instance.PlaySingle (popSound);
		popping = true;
		animator.SetTrigger ("playerPop");
		under = false;
	}

	private void OnTriggerStay2D (Collider2D other) {
		if (popping && other.tag == "Player" && !other.GetComponent<Player>().under && gameOver == false) {
			if (id == 1) {
				p1win = true;
				gameOver = true;
				Variables.pause = true;
			} else if (id == 2) {
				p1win = false;
				gameOver = true;
				Variables.pause = true;
			}
			SoundManager.instance.PlaySingle (winSound);
			other.GetComponent<Player>().animator.SetTrigger ("playerDie");
			Invoke ("Restart", restartDelay);
		}

		else if (popping && other.tag == "Rock" && gameOver == false) {
			if (id == 1) {
				p1win = false;
				gameOver = true;
				Variables.pause = true;
			} else if (id == 2) {
				p1win = true;
				gameOver = true;
				Variables.pause = true;
			}
			SoundManager.instance.PlaySingle (winSound);
			animator.SetTrigger ("playerDie");
			Invoke ("Restart", restartDelay);
		}
	}

	private void Restart() {
		if (p1win)
			Variables.p1Score++;
		else
			Variables.p2Score++;
		Application.LoadLevel (Application.loadedLevel);
	}

}
