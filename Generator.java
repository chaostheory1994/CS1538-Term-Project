
public interface Generator {
	public PowerType getPowerType();
	public double getPowerGen(double t);
	public double getGenTime();
	public boolean isWorking();
}
