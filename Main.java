import java.awt.AWTException;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import javax.swing.JFrame;

import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.joml.Vector3f;

import fastnoise.FastNoise;

public class Main {
	
	public JFrame frame;
	public BufferedImage image;
	
	public final int WIDTH = 1920, HEIGHT = 1080;
	public final int I_WIDTH = WIDTH / 4, I_HEIGHT = HEIGHT / 4;
	
	public final Point[] pixels;
	public final ArrayList<Point> pixel_list;
	public Random random;
	
	public Vector3d camera;
	public Vector3d look;
	public double near_plane;
	public double far_plane;
	public double fov;
	
	public Point mouse = new Point(0, 0);
	Point lastMouse = new Point(0, 0);
	
	public double sensitivity = 1;
	
	public List<Sphere> spheres = new ArrayList<Sphere>();
	public  List<Vector3d> boxes = new ArrayList<Vector3d>();
	
	public Vector3d sun;
	public FastNoise noise;
	
	public boolean captured = false;
	
	public boolean W, A, S, D, SPACE, SHIFT;
	
	public Color sky_color = Color.cyan;
	
	public Sphere SUN = new Sphere(new Vector3d(0, 1, 0), 10);
	
	public Main() {
		frame = new JFrame("test");
		frame.setSize(WIDTH / 2, HEIGHT / 2);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		frame.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				mouse.x = e.getX();
				mouse.y = e.getY();
			}
			
		});
		
		frame.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				
			}

			@Override
			public void mouseExited(MouseEvent e) {
				
			}

			@Override
			public void mousePressed(MouseEvent e) {
				captured = true;
				mouse.x = frame.getWidth() / 2;
				mouse.y = frame.getHeight() / 2;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				
			}
			
		});
		
		frame.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					captured = false;
				}
				if (e.getKeyCode() == KeyEvent.VK_W) W = true;
				if (e.getKeyCode() == KeyEvent.VK_A) A = true;
				if (e.getKeyCode() == KeyEvent.VK_S) S = true;
				if (e.getKeyCode() == KeyEvent.VK_D) D = true;
				if (e.getKeyCode() == KeyEvent.VK_SPACE) SPACE = true;
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) SHIFT = true;
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_W) W = false;
				if (e.getKeyCode() == KeyEvent.VK_A) A = false;
				if (e.getKeyCode() == KeyEvent.VK_S) S = false;
				if (e.getKeyCode() == KeyEvent.VK_D) D = false;
				if (e.getKeyCode() == KeyEvent.VK_SPACE) SPACE = false;
				if (e.getKeyCode() == KeyEvent.VK_SHIFT) SHIFT = false;
			}

			@Override
			public void keyTyped(KeyEvent e) {
				
			}
		
		});
		
		noise = new FastNoise();
		
		image = (BufferedImage)frame.createImage(I_WIDTH, I_HEIGHT);
		
		random = new Random();
		
		camera = new Vector3d(0, getHeight(0, 0) + 10, 0);
		look = new Vector3d(0, 0, 0);
		near_plane = 10;
		far_plane = 10;
		fov = 80;
		
		sun = new Vector3d(0, -1, 0);
		
		spheres.add(SUN);
		
		
		pixels = new Point[I_WIDTH * I_HEIGHT];
		pixel_list = new ArrayList<Point>();
		
		for (int i = 0; i < pixels.length; i++) {
			pixels[i] = new Point(i, 0);
		}
		pixel_list.addAll(Arrays.asList(pixels));
		
		while (true) {
			tick();
			render();
		}

	}
	
	
	public void tick() {
		if (captured) {
			lastMouse.x = frame.getWidth() / 2;
			lastMouse.y = frame.getHeight() / 2;
			
			if (look.x < -90) look.x = -90;
			if (look.x > 90) look.x = 90;
			look.x += (mouse.y - lastMouse.y) * sensitivity;
			look.y += (mouse.x - lastMouse.x) * sensitivity;
			
			if (mouse.x != lastMouse.x || mouse.y != lastMouse.y) {
				Robot robot = null;
				try {
					robot = new Robot();
				} catch (AWTException e) {
					e.printStackTrace();
				}
				if (robot != null) {
					robot.mouseMove(frame.getX() + frame.getWidth() / 2, frame.getY() + frame.getHeight() / 2);
					mouse.x = frame.getWidth() / 2;
					mouse.y = frame.getHeight() / 2;
				}
			}
			
			if (W) {
				camera.x += Math.sin(Math.toRadians(look.y));
				camera.z += Math.cos(Math.toRadians(look.y));
			}
			if (S) {
				camera.x -= Math.sin(Math.toRadians(look.y));
				camera.z -= Math.cos(Math.toRadians(look.y));
			}
			if (A) {
				camera.x += Math.sin(Math.toRadians(look.y - 90));
				camera.z += Math.cos(Math.toRadians(look.y - 90));
			}
			if (D) {
				camera.x += Math.sin(Math.toRadians(look.y + 90));
				camera.z += Math.cos(Math.toRadians(look.y + 90));
			}
			if (SPACE) {
				camera.y++;
			}
			if (SHIFT) {
				camera.y--;
			}
		}
	}
	
	public int mx, my;
	
	public double epsilon = 0.001;
	public void render() {
		Graphics g = frame.getGraphics();
		
		sun.rotateZ(0.01f);
		
		Vector3d cam = new Vector3d(camera);
		Vector3d dir = new Vector3d(look);
		Vector3d sun_dir = new Vector3d(sun);
		
		Quaterniond quat = new Quaterniond();
		quat.identity();
		
		quat.rotateLocalX(Math.toRadians(look.x));
		quat.rotateLocalY(Math.toRadians(look.y));
		
		Stream<Point> pixel_stream = pixel_list.parallelStream().parallel();
		pixel_stream.forEach(i -> {
			int index = i.x;
			int x = index % I_WIDTH;
			int y = (index - x) / I_WIDTH;
			
			
			int color = raymarch(x, y, cam, dir, sun_dir, quat);
			image.setRGB(x, y, color);
		});
		pixel_stream.close();
		g.drawImage(image, 0, frame.getHeight(), frame.getWidth(), -frame.getHeight(), null);
		g.dispose();
	}
	
	public int raymarch(int x, int y, Vector3d camera, Vector3d look, Vector3d sun, Quaterniond quat) {
		
		//(FOV / 2.0) * ((y - (size / 2)) / (float)(size / 2)))
		double X = x / (double)I_WIDTH - 0.5;
		double Y = y / (double)I_HEIGHT - 0.5;
		
		Vector3d point = new Vector3d(0, 0, near_plane);
		
		Vector3d dir = new Vector3d(X, Y, 1.0);
		
		
		dir.rotate(quat);
		point.rotate(quat);
		point.add(camera);
		
		double hit = march_trace(point, dir, far_plane * 1000);
		if (hit != -1) {
			Vector3d sun_dir = new Vector3d(sun).mul(-1);
			
			point.add(new Vector3d(dir).mul(hit));
			
			Vector3d normal = estimateNormal(point);
			
			point.add(new Vector3d(normal).mul(epsilon));
			
			double dot = new Vector3d(sun_dir).dot(normal);
			dot += 1;
			dot /= 2;
			double hit_sun = 1;
			double dist = point.distance(camera);
			hit_sun = march_trace(point, sun_dir, far_plane * 10);
			
			dot *= 10000;
			dot -= 5000;
			
			dot = Math.max(0, Math.min(1, dot));
			dot += 1.0;
			dot /= 2.0;
			if (hit_sun != -1) {
				dot = 0.5;
			}
			
			dot /= 2.0f;
			
			double r = dot;
			double g = dot;
			double b = dot;
			
			
			
			if (point.y <= 10) {
				//b += 1.0;
				//b /= 2.0;
				
				r += 1;
				r /= 2.0;
				g += 1;
				g /= 2.0;
				
				if (point.y <= 6) {
					b += 1.0;
					b /= 2.0;
					
					double water = getWaterNoise(point.x, point.z);
					
					water += 1;
					water /= 2.0;
					water = Math.max(0, Math.min(water, 1));
					water *= 0.5;
					r += water;
					r /= 2.0;
					g += water;
					g /= 2.0;
					r /= 2.0;
					g /= 2.0;
				} else {
					double texture = 1;
					if (dist < 300) {
						texture = getTexture(point.x, point.z);
					}
					texture += 1;
					texture /= 2.0;
					texture = Math.max(0.5, Math.min(texture, 1));
					r += texture;
					g += texture;
					b += texture;
					r *= 0.5;
					g *= 0.5;
					b *= 0.5;
				}
				
			} else {
				if (point.y % 1 < 0.1f) {
					g += 1.0;
					g /= 2.0;
					
					double texture = 1;
					if (dist < 300) {
						texture = getTexture(point.x, point.z);
					}
					texture += 1;
					texture /= 2.0;
					texture = Math.max(0.5, Math.min(texture, 1)) * 0.5;
					r += texture;
					g += texture;
					b += texture;
					r *= 0.5;
					g *= 0.5;
					b *= 0.5;
					
				} else {
					
					double texture = 1;
					if (dist < 200) {
						texture = getTexture(point.x, point.z);
						r += 1.0;
						r /= 2.0;
					} else {
						g += 1.0;
						g /= 2.0;
					}
					texture += 1;
					texture /= 2.0;
					texture = Math.max(0.5, Math.min(texture, 1)) * 0.1;
					r += texture;
					g += texture;
					b += texture;
					r *= 0.5;
					g *= 0.5;
					b *= 0.5;
				}
			}
			
			Color sunlight = new Color((float)r, (float)g, (float)b);
			
			
			return sunlight.getRGB();
		}
		return sky_color.getRGB();
	}
		
	public double march_trace(Vector3d start, Vector3d start_dir, double far) {
		
		double depth = 0;
		
		while (depth < far) {
			double dist = sceneSDF(new Vector3d(start).add(new Vector3d(start_dir).mul(depth)));
			
			if (dist < epsilon) {
				return depth;
			}
			
			if (dist > 100) {
				if (dist > 200) {
					if (dist > 400) {
						depth += dist * 2;
					} else {
						depth += dist;
					}
				} else {
					depth += dist / 2;
				}
			} else {
				depth += dist / 3;
			}
			
			
		}
		
		return -1;
	}
	
	double sceneSDF(Vector3d p) {
		double sdf = groundSDF(p);
		
		return sdf;
	}
	
	double max(double a, double b) {
		return a > b ? a : b;
	}
	
	double min(double a, double b) {
		return a < b ? a : b;
	}
	
	double intersectSDF(double distA, double distB) {
	    return max(distA, distB);
	}

	double unionSDF(double distA, double distB) {
	    return min(distA, distB);
	}

	double differenceSDF(double distA, double distB) {
	    return max(distA, -distB);
	}
	
	double sphereSDF(Vector3d p, Vector3d pos, double radius) {
	    return p.distance(pos) - radius;
	}
	
	double boxSDF( Vector3d p, Vector3d b )
	{
		Vector3d q = new Vector3d(p).absolute().sub(b);
	  return new Vector3d(max(q.x, 0), max(q.y, 0), max(q.z, 0)).length() + min(max(q.x,max(q.y,q.z)),0.0);
	}
	
	double groundSDF(Vector3d p) {
		
		float height = getHeight((int)Math.round(p.x), (int)Math.round(p.z)) - 10;
		return p.y + Math.round(height);
	}
	
	public double[] intersectBox(Vector3d p) {
		  double x1 = Math.floor(p.x) - p.x;
		  double y1 = Math.floor(p.y) - p.y;
		  double z1 = Math.floor(p.z) - p.z;
		  
		  double x2 = Math.ceil(p.x) - p.x;
		  double y2 = Math.ceil(p.y) - p.y;
		  double z2 = Math.ceil(p.z) - p.z;
		  
		  double min_x = min(x1, x2);
		  double min_y = min(y1, y2);
		  double min_z = min(z1, z2);
		  
		  double max_x = max(x1, x2);
		  double max_y = max(y1, y2);
		  double max_z = max(z1, z2);
		  
		  double tNear = max(max(min_x, min_y), min_z);
		  double tFar = min(min(min_x, min_y), min_z);
		  return new double[] {tNear, tFar};
	}
	
	int getHeight(int x, int y) {
		double height = (int)(noise.GetNoise(x, y) * 15 + getHeightMod(x, y));
		return (int)(height);
	}
	
	double getHeightMod(int x, int y) {
		double height = noise.GetNoise((float)(x / 10.0), (float)(y / 10.0));
		return height * 50;
	}
	
	double getWaterNoise(double x, double y) {
		double height = noise.GetSimplex((float)(x * 160), (float)(y * 160), (float)(System.nanoTime() / 25000000.0));
		return height;
	}
	
	double getTexture(double x, double y) {
		double height = noise.GetSimplex((float)(x * 800), (float)(y * 800));
		return height;
	}
	
	Vector3d estimateNormal(Vector3d p) {
		return new Vector3d(
				
				sceneSDF(new Vector3d(p.x + epsilon, p.y, p.z))	- sceneSDF(new Vector3d(p.x - epsilon, p.y, p.z)),			
				sceneSDF(new Vector3d(p.x, p.y + epsilon, p.z))	- sceneSDF(new Vector3d(p.x, p.y - epsilon, p.z)),			
				sceneSDF(new Vector3d(p.x, p.y, p.z + epsilon))	- sceneSDF(new Vector3d(p.x, p.y, p.z - epsilon))			
				).normalize();
	}
	
	public static void main(String[] args) {
		new Main();
	}
}
