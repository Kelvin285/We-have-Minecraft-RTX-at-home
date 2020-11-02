import org.joml.Vector3d;

public class Sphere {
	public Vector3d pos;
	public double radius;
	public Sphere(Vector3d pos, double radius) {
		this.pos = new Vector3d(pos.x, pos.y, pos.z);
		this.radius = radius;
	}
}
