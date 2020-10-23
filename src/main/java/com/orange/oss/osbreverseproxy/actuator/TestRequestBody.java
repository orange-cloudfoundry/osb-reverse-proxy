package com.orange.oss.osbreverseproxy.actuator;

import java.util.function.Predicate;

import org.springframework.stereotype.Component;

//See https://stackoverflow.com/a/64080867/1484823
@Component
public class TestRequestBody implements Predicate
{
	@Override
	public boolean test(Object o)
	{
		return true;
	}
}