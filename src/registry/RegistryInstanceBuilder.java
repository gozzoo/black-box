package registry;

public interface RegistryInstanceBuilder<T> {
	public T createInstance() throws Exception;
}
