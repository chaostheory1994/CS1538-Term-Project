
public interface Generator {
	public PowerType getPowerType();
	public double getPowerGen(float t);
	public double getGenTime();
	public boolean isWorking();
}
