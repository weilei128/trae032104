import requests
import json

BASE_URL = "http://localhost:10101"

def test_admin_login():
    """测试管理员登录"""
    print("=" * 50)
    print("测试: 管理员登录")
    print("=" * 50)
    
    url = f"{BASE_URL}/login"
    data = {
        "username": "admin",
        "password": "admin123"
    }
    
    try:
        response = requests.post(url, data=data, timeout=10)
        print(f"状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        
        if response.status_code == 200:
            result = response.json()
            if result.get("success"):
                print("✓ 管理员登录成功!")
                print(f"  Token: {result.get('token')[:50]}...")
                print(f"  管理员权限: {result.get('admin')}")
                return result.get("token")
            else:
                print(f"✗ 登录失败: {result.get('message')}")
        else:
            print(f"✗ 请求失败，状态码: {response.status_code}")
    except Exception as e:
        print(f"✗ 请求异常: {e}")
    return None

def test_user_login():
    """测试普通用户登录"""
    print("=" * 50)
    print("测试: 普通用户登录")
    print("=" * 50)
    
    url = f"{BASE_URL}/login"
    data = {
        "username": "user1",
        "password": "user123"
    }
    
    try:
        response = requests.post(url, data=data, timeout=10)
        print(f"状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        
        if response.status_code == 200:
            result = response.json()
            if result.get("success"):
                print("✓ 普通用户登录成功!")
                print(f"  Token: {result.get('token')[:50]}...")
                print(f"  管理员权限: {result.get('admin')}")
                return result.get("token")
            else:
                print(f"✗ 登录失败: {result.get('message')}")
        else:
            print(f"✗ 请求失败，状态码: {response.status_code}")
    except Exception as e:
        print(f"✗ 请求异常: {e}")
    return None

def test_wrong_password():
    """测试错误密码"""
    print("=" * 50)
    print("测试: 错误密码登录")
    print("=" * 50)
    
    url = f"{BASE_URL}/login"
    data = {
        "username": "admin",
        "password": "wrongpassword"
    }
    
    try:
        response = requests.post(url, data=data, timeout=10)
        print(f"状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        
        if response.status_code == 200:
            result = response.json()
            if not result.get("success"):
                print("✓ 正确拒绝错误密码登录!")
                print(f"  错误信息: {result.get('message')}")
            else:
                print("✗ 错误密码居然登录成功了!")
        else:
            print(f"✗ 请求失败，状态码: {response.status_code}")
    except Exception as e:
        print(f"✗ 请求异常: {e}")

def test_wrong_username():
    """测试不存在的用户名"""
    print("=" * 50)
    print("测试: 不存在的用户名登录")
    print("=" * 50)
    
    url = f"{BASE_URL}/login"
    data = {
        "username": "nonexistentuser",
        "password": "anypassword"
    }
    
    try:
        response = requests.post(url, data=data, timeout=10)
        print(f"状态码: {response.status_code}")
        print(f"响应内容: {response.text}")
        
        if response.status_code == 200:
            result = response.json()
            if not result.get("success"):
                print("✓ 正确拒绝不存在的用户名!")
                print(f"  错误信息: {result.get('message')}")
            else:
                print("✗ 不存在的用户名居然登录成功了!")
        else:
            print(f"✗ 请求失败，状态码: {response.status_code}")
    except Exception as e:
        print(f"✗ 请求异常: {e}")

def test_access_admin_page(token):
    """测试访问管理员页面"""
    print("=" * 50)
    print("测试: 使用Token访问管理员页面")
    print("=" * 50)
    
    url = f"{BASE_URL}/admin/products"
    headers = {
        "Authorization": f"Bearer {token}"
    }
    
    try:
        response = requests.get(url, headers=headers, timeout=10, allow_redirects=False)
        print(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            print("✓ 成功访问管理员页面!")
        elif response.status_code == 302:
            print("! 被重定向，可能需要检查页面渲染")
            # 再试一次，允许重定向
            response = requests.get(url, headers=headers, timeout=10)
            if response.status_code == 200 and "产品管理" in response.text:
                print("✓ 成功访问管理员页面(重定向后)!")
        else:
            print(f"✗ 访问失败，状态码: {response.status_code}")
            print(f"响应内容: {response.text[:200]}")
    except Exception as e:
        print(f"✗ 请求异常: {e}")

def test_access_shop_page(token):
    """测试访问商城页面"""
    print("=" * 50)
    print("测试: 使用Token访问商城页面")
    print("=" * 50)
    
    url = f"{BASE_URL}/shop"
    headers = {
        "Authorization": f"Bearer {token}"
    }
    
    try:
        response = requests.get(url, headers=headers, timeout=10, allow_redirects=False)
        print(f"状态码: {response.status_code}")
        
        if response.status_code == 200:
            print("✓ 成功访问商城页面!")
        elif response.status_code == 302:
            print("! 被重定向，可能需要检查页面渲染")
            response = requests.get(url, headers=headers, timeout=10)
            if response.status_code == 200 and "商品列表" in response.text:
                print("✓ 成功访问商城页面(重定向后)!")
        else:
            print(f"✗ 访问失败，状态码: {response.status_code}")
            print(f"响应内容: {response.text[:200]}")
    except Exception as e:
        print(f"✗ 请求异常: {e}")

def test_access_without_token():
    """测试未携带Token访问受保护页面"""
    print("=" * 50)
    print("测试: 未携带Token访问受保护页面")
    print("=" * 50)
    
    url = f"{BASE_URL}/shop"
    
    try:
        response = requests.get(url, timeout=10, allow_redirects=False)
        print(f"状态码: {response.status_code}")
        
        if response.status_code == 401:
            print("✓ 正确拒绝未授权访问!")
        else:
            print(f"! 状态码: {response.status_code}，响应: {response.text[:200]}")
    except Exception as e:
        print(f"✗ 请求异常: {e}")

def main():
    print("\n开始执行登录测试...\n")
    
    # 测试1: 错误密码
    test_wrong_password()
    print()
    
    # 测试2: 不存在的用户名
    test_wrong_username()
    print()
    
    # 测试3: 未授权访问
    test_access_without_token()
    print()
    
    # 测试4: 管理员登录
    admin_token = test_admin_login()
    print()
    
    if admin_token:
        # 测试5: 管理员访问管理页面
        test_access_admin_page(admin_token)
        print()
    
    # 测试6: 普通用户登录
    user_token = test_user_login()
    print()
    
    if user_token:
        # 测试7: 普通用户访问商城页面
        test_access_shop_page(user_token)
        print()
    
    print("=" * 50)
    print("测试完成!")
    print("=" * 50)

if __name__ == "__main__":
    main()
