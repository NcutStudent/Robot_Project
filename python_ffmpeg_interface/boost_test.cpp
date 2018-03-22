char const* greet()
{
   return "hello, world";
}
#include <boost/python.hpp>
#include <boost/python/numpy.hpp>
#include <vector>

using namespace std;
namespace bp = boost::python;
namespace bn = boost::python::numpy;

bn::ndarray mywrapper() {
    std::vector<double> v = {0, 1, 2, 3, 4};
    Py_intptr_t shape[1] = { v.size() };
    bn::ndarray result = bn::zeros(1, shape, bn::dtype::get_builtin<double>());
    std::copy(v.begin(), v.end(), reinterpret_cast<double*>(result.get_data()));
    return result;
}

bn::ndarray mywrapper1(bn::ndarray a) {
    return a;
}


BOOST_PYTHON_MODULE(libboost_test)
{
    bn::initialize();
    using namespace boost::python;
    def("greet", greet);
    def("myfunc", mywrapper1);
}
/*
#include <boost/python/numpy.hpp>

namespace bp = boost::python;
namespace bn = boost::python::numpy;

bn::ndarray mywrapper() {
    std::vector<double> v = {0, 1, 2, 3, 4};
    Py_intptr_t shape[1] = { v.size() };
    bn::ndarray result = bn::zeros(1, shape, bn::dtype::get_builtin<double>());
    std::copy(v.begin(), v.end(), reinterpret_cast<double*>(result.get_data()));
    return result;
}

BOOST_PYTHON_MODULE(libboost_test) {
    bn::initialize();
    bp::def("myfunc", mywrapper);
}
int main(){
}
*/
